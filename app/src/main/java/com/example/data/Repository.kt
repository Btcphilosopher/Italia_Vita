package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val reservationDao = db.reservationDao()
    private val profileDao = db.customerProfileDao()
    private val orderDao = db.tableOrderDao()
    private val ticketDao = db.eventTicketDao()

    // Exposed Flows
    val reservations: Flow<List<Reservation>> = reservationDao.getAllReservations()
    val customerProfile: Flow<CustomerProfile?> = profileDao.getProfileFlow()
    val cartItems: Flow<List<TableOrderItem>> = orderDao.getAllOrderItems()
    val tickets: Flow<List<EventTicket>> = ticketDao.getAllTickets()

    // Pre-seeding status tracker
    private val _isSeeded = MutableStateFlow(false)
    val isSeeded = _isSeeded.asStateFlow()

    init {
        // Run pre-seeding on background thread
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
            _isSeeded.value = true
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        // 1. Seed Customer Profile
        val currentProfile = profileDao.getProfileDirect()
        if (currentProfile == null) {
            val defaultProfile = CustomerProfile(
                customerId = "vita_member_501",
                customerName = "Tommaso Rossini",
                memberLevel = "Oro", // Gold member level
                loyaltyPoints = 2450,
                favouriteRestaurantId = "baglioni_milan",
                favouriteDishName = "Risotto alla Milanese",
                email = "tom@ahyx.org"
            )
            profileDao.insertProfile(defaultProfile)
        }

        // 2. See if reservations are empty, and seed the dynamic history/ongoing booking
        val reservationCount = reservationDao.getCount()
        if (reservationCount == 0) {
            // A past reservation that shows in history
            val pastBkg = Reservation(
                restaurantId = "palagio_florence",
                restaurantName = "Il Palagio di Firenze",
                city = "Florence",
                bookingDate = "June 10, 2026",
                bookingTime = "19:30",
                partySize = 4,
                seatingArea = "Terrace Garden",
                specialRequests = "Gluten-free menu requested for one guest.",
                status = "Completed",
                timestamp = System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000 // 6 days ago
            )
            // A future reservation that stays active
            val activeBkg = Reservation(
                restaurantId = "baglioni_milan",
                restaurantName = "Caffè Baglioni & Giardino",
                city = "Milan",
                bookingDate = "June 23, 2026",
                bookingTime = "20:30",
                partySize = 2,
                seatingArea = "Sommelier Cellar",
                specialRequests = "Celebrating 10th anniversary. Table near acoustic performance if possible.",
                status = "Confirmed",
                timestamp = System.currentTimeMillis()
            )
            reservationDao.insertReservation(pastBkg)
            reservationDao.insertReservation(activeBkg)
        }
    }

    // --- RESERVATION TRANSACTIONS ---

    suspend fun createReservation(
        restaurantId: String,
        restaurantName: String,
        city: String,
        bookingDate: String,
        bookingTime: String,
        partySize: Int,
        seatingArea: String,
        specialRequests: String
    ): Reservation = withContext(Dispatchers.IO) {
        val r = Reservation(
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            city = city,
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            partySize = partySize,
            seatingArea = seatingArea,
            specialRequests = specialRequests,
            status = "Confirmed",
            timestamp = System.currentTimeMillis()
        )
        reservationDao.insertReservation(r)
        
        // Add 100 Club Italia loyalty points for scheduling a new luxurious table reservation!
        profileDao.addPoints(100)
        
        r
    }

    suspend fun cancelReservation(reservation: Reservation) = withContext(Dispatchers.IO) {
        val updated = reservation.copy(status = "Cancelled")
        reservationDao.insertReservation(updated) // updates as primaryKey ID is retained
    }

    suspend fun joinWaitingList(
        restaurantId: String,
        restaurantName: String,
        city: String,
        bookingDate: String,
        bookingTime: String,
        partySize: Int,
        seatingArea: String
    ): Reservation = withContext(Dispatchers.IO) {
        val r = Reservation(
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            city = city,
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            partySize = partySize,
            seatingArea = seatingArea,
            specialRequests = "Waitlist Enrollment",
            status = "Waiting List",
            timestamp = System.currentTimeMillis()
        )
        reservationDao.insertReservation(r)
        profileDao.addPoints(50) // 50 points for waitlist devotion
        r
    }

    // --- PROFILE TRANSACTIONS ---

    suspend fun updateFavorite(dishName: String, restaurantId: String) = withContext(Dispatchers.IO) {
        val current = profileDao.getProfileDirect() ?: CustomerProfile()
        val updated = current.copy(
            favouriteDishName = dishName,
            favouriteRestaurantId = restaurantId
        )
        profileDao.insertProfile(updated)
    }

    suspend fun awardPoints(points: Int) = withContext(Dispatchers.IO) {
        profileDao.addPoints(points)
    }

    // --- FOOD ORDERING & TABLE SERVICE CART ---

    suspend fun addToCart(dish: Dish, quantity: Int = 1, isTableService: Boolean) = withContext(Dispatchers.IO) {
        val item = TableOrderItem(
            dishName = dish.name,
            category = dish.category,
            price = dish.price,
            quantity = quantity,
            isTableService = isTableService
        )
        orderDao.insertOrderItem(item)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        orderDao.clearCart()
    }

    // Submit table orders and add loyalty points
    suspend fun submitCartAsOrder(pointsPerEuro: Int = 5) = withContext(Dispatchers.IO) {
        // Collect items
        // Since we are working in Dispatchers.IO, let's run db cart transactions
        // Clear cart, award corresponding member points
        orderDao.clearCart()
        profileDao.addPoints(150) // award flat 150 Club Italia points
    }

    // --- EVENT TICKETS TRANSACTIONS ---

    suspend fun buyEventTicket(event: HospitalityEvent, ticketCount: Int): EventTicket = withContext(Dispatchers.IO) {
        val total = event.price * ticketCount
        val ticket = EventTicket(
            eventId = event.id,
            eventTitle = event.title,
            location = event.location,
            date = event.date,
            ticketCount = ticketCount,
            totalPaid = total
        )
        ticketDao.insertTicket(ticket)
        profileDao.addPoints((total * 1.5).toInt()) // 1.5 points per euro spent on exclusive masterclasses
        ticket
    }
}
