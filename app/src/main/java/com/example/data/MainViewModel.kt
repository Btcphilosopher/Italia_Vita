package com.example.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppTab {
    HOME,
    LOCATIONS,
    MENU,
    CELLAR,
    CLUB_ITALIA,
    JOURNAL,
    PRIVATE_DINING,
    GIFTING
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)

    // Reactively observe our database updates
    val reservations = repository.reservations
    val customerProfile = repository.customerProfile
    val cartItems = repository.cartItems
    val tickets = repository.tickets
    val isSeeded = repository.isSeeded

    // --- UI NAVIGATION STATE ---
    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- SELECTION & DETAIL FLOWS ---
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant = _selectedRestaurant.asStateFlow()

    fun selectRestaurant(restaurant: Restaurant?) {
        _selectedRestaurant.value = restaurant
    }

    // Used to pre-fill the order/booking screen with a selected dining area or category
    private val _selectedSeatingArea = MutableStateFlow("Indoor Salon")
    val selectedSeatingArea = _selectedSeatingArea.asStateFlow()

    fun selectSeatingArea(area: String) {
        _selectedSeatingArea.value = area
    }

    // --- AI CONCIERGE & SOMMELIER SLATE STATE ---
    private val _conciergeMessage = MutableStateFlow("")
    val conciergeMessage = _conciergeMessage.asStateFlow()

    private val _isLoadingConcierge = MutableStateFlow(false)
    val isLoadingConcierge = _isLoadingConcierge.asStateFlow()

    fun triggerConciergeAdvice() {
        viewModelScope.launch {
            _isLoadingConcierge.value = true
            val profile = customerProfile.firstOrNull() ?: CustomerProfile()
            val event = StaticData.hospitalityEvents.firstOrNull()?.title ?: "Exclusive Wine Pairing"
            val advice = GeminiService.getConciergeAdvice(profile, event)
            _conciergeMessage.value = advice
            _isLoadingConcierge.value = false
        }
    }

    private val _sommelierRecommendation = MutableStateFlow("")
    val sommelierRecommendation = _sommelierRecommendation.asStateFlow()

    private val _isLoadingSommelier = MutableStateFlow(false)
    val isLoadingSommelier = _isLoadingSommelier.asStateFlow()

    private val _matchedDishName = MutableStateFlow("")
    val matchedDishName = _matchedDishName.asStateFlow()

    fun requestSommelierPairing(dish: Dish) {
        _matchedDishName.value = dish.name
        viewModelScope.launch {
            _isLoadingSommelier.value = true
            val advice = GeminiService.getSommelierWinePairing(dish.name, dish.ingredients)
            _sommelierRecommendation.value = advice
            _isLoadingSommelier.value = false
        }
    }

    // --- RESERVATION TRANSACTIONS ---
    fun makeNewReservation(
        restaurantId: String,
        restaurantName: String,
        city: String,
        date: String,
        time: String,
        partySize: Int,
        seatingArea: String,
        specialRequests: String
    ) {
        viewModelScope.launch {
            repository.createReservation(
                restaurantId, restaurantName, city, date, time, partySize, seatingArea, specialRequests
            )
            // Go to Club Italia to inspect the new active reservation
            _currentTab.value = AppTab.CLUB_ITALIA
        }
    }

    fun bookChefsTable(
        city: String,
        date: String,
        time: String,
        partySize: Int,
        includeWineFlight: Boolean,
        dietaryRestrictions: String
    ) {
        viewModelScope.launch {
            val restaurantId = "chefs_table_${city.replace(" ", "_").lowercase()}"
            val restaurantName = "La Tavola dello Chef - $city"
            val wineFlightStr = if (includeWineFlight) "Prestige Wine Flight Included (+€110/guest)" else "Wine Flight Declined"
            val specialRequests = "Intimate Tasting Table. Pairing: $wineFlightStr. Dietary Notes: $dietaryRestrictions"
            repository.createReservation(
                restaurantId = restaurantId,
                restaurantName = restaurantName,
                city = city,
                bookingDate = date,
                bookingTime = time,
                partySize = partySize,
                seatingArea = "Chef's Table (VIP Seating)",
                specialRequests = specialRequests
            )
            repository.awardPoints(500) // VIP Experience Bonus Points!
            _currentTab.value = AppTab.CLUB_ITALIA
        }
    }

    fun makeWaitlistEnrollment(
        restaurantId: String,
        restaurantName: String,
        city: String,
        date: String,
        time: String,
        partySize: Int,
        seatingArea: String
    ) {
        viewModelScope.launch {
            repository.joinWaitingList(
                restaurantId, restaurantName, city, date, time, partySize, seatingArea
            )
            _currentTab.value = AppTab.CLUB_ITALIA
        }
    }

    fun cancelActiveReservation(reservation: Reservation) {
        viewModelScope.launch {
            repository.cancelReservation(reservation)
        }
    }

    // --- TABLE SIDE ORDERING ACTIONS ---
    private val _isTableModeActive = MutableStateFlow(false)
    val isTableModeActive = _isTableModeActive.asStateFlow()

    private val _assignedTableNumber = MutableStateFlow<Int?>(null)
    val assignedTableNumber = _assignedTableNumber.asStateFlow()

    fun toggleTableMode(active: Boolean, table: Int? = null) {
        _isTableModeActive.value = active
        _assignedTableNumber.value = table
    }

    fun addDishToCart(dish: Dish, quantity: Int = 1, isTableService: Boolean) {
        viewModelScope.launch {
            repository.addToCart(dish, quantity, isTableService)
        }
    }

    fun clearCartItems() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun submitCurrentOrder() {
        viewModelScope.launch {
            repository.submitCartAsOrder()
            // Add custom notification or toast in active table flow
        }
    }

    // --- EVENT BOOKINGS & PURCHASES ---
    fun purchaseEventTicket(event: HospitalityEvent, ticketCount: Int) {
        viewModelScope.launch {
            repository.buyEventTicket(event, ticketCount)
            // Go to club Italia to review ticket QR code
            _currentTab.value = AppTab.CLUB_ITALIA
        }
    }

    // --- WALLET GIFT CARDS ---
    private val _purchasedGiftCards = MutableStateFlow<List<String>>(emptyList())
    val purchasedGiftCards = _purchasedGiftCards.asStateFlow()

    fun buyGiftCard(amount: Double, recipient: String, message: String) {
        viewModelScope.launch {
            val code = "VITA-GIFT-${(1000..9999).random()}"
            _purchasedGiftCards.value = _purchasedGiftCards.value + "€$amount Card to $recipient ($code)"
            repository.awardPoints((amount * 0.5).toInt()) // 0.5 points per euro spent on gifts
        }
    }

    // --- PRIVATE EVENT INQUIRIES ---
    private val _privateInquiries = MutableStateFlow<List<String>>(emptyList())
    val privateInquiries = _privateInquiries.asStateFlow()

    fun submitPrivateEventInquiry(city: String, restaurant: String, date: String, guests: Int, notes: String) {
        viewModelScope.launch {
            val inquiry = "Inquiry filed: $restaurant ($city) on $date for $guests guests. Notes: $notes"
            _privateInquiries.value = _privateInquiries.value + inquiry
        }
    }

    init {
        // Trigger the initial concierge advice on startup!
        triggerConciergeAdvice()
    }
}
