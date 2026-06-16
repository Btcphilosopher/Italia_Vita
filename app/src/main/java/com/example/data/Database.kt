package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- DAOs ---

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY timestamp DESC")
    fun getAllReservations(): Flow<List<Reservation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: Reservation)

    @Update
    suspend fun updateReservation(reservation: Reservation)

    @Delete
    suspend fun deleteReservation(reservation: Reservation)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM reservations")
    suspend fun getCount(): Int
}

@Dao
interface CustomerProfileDao {
    @Query("SELECT * FROM customer_profile LIMIT 1")
    fun getProfileFlow(): Flow<CustomerProfile?>

    @Query("SELECT * FROM customer_profile LIMIT 1")
    suspend fun getProfileDirect(): CustomerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CustomerProfile)

    @Query("UPDATE customer_profile SET loyaltyPoints = loyaltyPoints + :points")
    suspend fun addPoints(points: Int)
}

@Dao
interface TableOrderDao {
    @Query("SELECT * FROM table_order_items ORDER BY orderTime DESC")
    fun getAllOrderItems(): Flow<List<TableOrderItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: TableOrderItem)

    @Query("DELETE FROM table_order_items")
    suspend fun clearCart()
}

@Dao
interface EventTicketDao {
    @Query("SELECT * FROM masterclass_tickets ORDER BY id DESC")
    fun getAllTickets(): Flow<List<EventTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: EventTicket)
}

// --- DATABASE CLASS ---

@Database(entities = [Reservation::class, CustomerProfile::class, TableOrderItem::class, EventTicket::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservationDao(): ReservationDao
    abstract fun customerProfileDao(): CustomerProfileDao
    abstract fun tableOrderDao(): TableOrderDao
    abstract fun eventTicketDao(): EventTicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "italia_vita_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
