package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val profile by viewModel.customerProfile.collectAsState(initial = null)
    val reservations by viewModel.reservations.collectAsState(initial = emptyList())
    val tableModeActive by viewModel.isTableModeActive.collectAsState()
    val tableNumber by viewModel.assignedTableNumber.collectAsState()

    var showSommelierDialog by remember { mutableStateOf(false) }
    var selectedDishForSommelier by remember { mutableStateOf<Dish?>(null) }
    var showChefsTableDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalActivity,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ITALIA VITA",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = DeepBurgundy,
                            letterSpacing = 4.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LuxuryCream,
                    titleContentColor = DeepBurgundy
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.selectTab(AppTab.CLUB_ITALIA) },
                        modifier = Modifier.testTag("top_bar_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = "Profile",
                            tint = DeepBurgundy
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = LuxuryCream,
                contentColor = DeepBurgundy,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                val tabs = listOf(
                    Triple(AppTab.HOME, Icons.Filled.Home, "Piazza"),
                    Triple(AppTab.LOCATIONS, Icons.Filled.Room, "Locations"),
                    Triple(AppTab.MENU, Icons.Filled.RestaurantMenu, "Menu"),
                    Triple(AppTab.CELLAR, Icons.Filled.WineBar, "Cellar"),
                    Triple(AppTab.CLUB_ITALIA, Icons.Filled.CreditCard, "Club Italia")
                )

                tabs.forEach { (tab, icon, label) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) DeepBurgundy else SoftBronze
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DeepBurgundy else SoftBronze
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = LuxuryGold.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = LuxuryCream
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "tab_transitions"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.HOME -> HomeScreenView(
                        viewModel = viewModel,
                        onDishSelect = { dish ->
                            selectedDishForSommelier = dish
                            showSommelierDialog = true
                            viewModel.requestSommelierPairing(dish)
                        },
                        onChefsTableClick = {
                            showChefsTableDialog = true
                        }
                    )
                    AppTab.LOCATIONS -> LocatorScreenView(viewModel = viewModel)
                    AppTab.MENU -> MenuScreenView(
                        viewModel = viewModel,
                        onConsultSommelier = { dish ->
                            selectedDishForSommelier = dish
                            showSommelierDialog = true
                            viewModel.requestSommelierPairing(dish)
                        }
                    )
                    AppTab.CELLAR -> WineCellarView(viewModel = viewModel)
                    AppTab.CLUB_ITALIA -> ClubItaliaView(viewModel = viewModel)
                    AppTab.JOURNAL -> JournalScreenView(viewModel = viewModel)
                    AppTab.PRIVATE_DINING -> PrivateDiningView(viewModel = viewModel)
                    AppTab.GIFTING -> GiftingPlatformView(viewModel = viewModel)
                }
            }

            // Sommelier pairing dialogue
            if (showSommelierDialog && selectedDishForSommelier != null) {
                SommelierPairingDialog(
                    dish = selectedDishForSommelier!!,
                    viewModel = viewModel,
                    onDismiss = { showSommelierDialog = false }
                )
            }

            // Chef's Table Booking Dialog
            if (showChefsTableDialog) {
                ChefsTableBookingDialog(
                    viewModel = viewModel,
                    onDismiss = { showChefsTableDialog = false }
                )
            }
        }
    }
}

// ------ SUB-VIEWS ------

@Composable
fun HomeScreenView(
    viewModel: MainViewModel,
    onDishSelect: (Dish) -> Unit,
    onChefsTableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.customerProfile.collectAsState(initial = null)
    val conciergeMsg by viewModel.conciergeMessage.collectAsState()
    val isLoadingConcierge by viewModel.isLoadingConcierge.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Piazza Sunset Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("home_hero_banner")
            ) {
                // Editorial image of spectacular sunset Duomo Milan
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f",
                    contentDescription = "Milan sunset",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Elegant dark overlay with a warm sunset golden gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Benvenuto a Casa Vostra",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Step into modern Italian hospitality, fine wines and curated local estates.",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        color = OffWhite,
                        maxLines = 2
                    )
                }
            }
        }

        // Live AI Butler Concierge panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_concierge_panel"),
                colors = CardDefaults.cardColors(containerColor = CharcoalDarkBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONCIERGE VITA",
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.triggerConciergeAdvice() },
                            modifier = Modifier.size(28.dp).testTag("refresh_concierge_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = LuxuryGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoadingConcierge) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = LuxuryGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Consulting our house butler...",
                                color = OffWhite,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Text(
                            text = conciergeMsg,
                            color = OffWhite,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Serif,
                            style = LocalTextStyle.current.copy(lineHeight = 18.sp)
                        )
                    }
                }
            }
        }

        // Quick Luxury Entry Nodes Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftIvoryCard)
                        .clickable { viewModel.selectTab(AppTab.LOCATIONS) }
                        .padding(12.dp)
                        .testTag("action_book_table"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Outlined.BookOnline, contentDescription = null, tint = DeepBurgundy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Book Table", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBurgundy)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftIvoryCard)
                        .clickable { viewModel.selectTab(AppTab.CELLAR) }
                        .padding(12.dp)
                        .testTag("action_sommelier"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Outlined.WineBar, contentDescription = null, tint = DeepBurgundy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AI Sommelier", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBurgundy)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftIvoryCard)
                        .clickable { viewModel.selectTab(AppTab.JOURNAL) }
                        .padding(12.dp)
                        .testTag("action_journal"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Outlined.MenuBook, contentDescription = null, tint = DeepBurgundy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Journal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBurgundy)
                }
            }
        }

        // Seasonal Michelin Specials
        item {
            Column {
                Text(
                    text = "L'Arte della Cucina",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Seasonal Michelin specials curated by Chef Marco",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("michelin_specials_row")
            ) {
                // Display 3 primary dishes
                items(StaticData.menuDishes.take(3)) { dish ->
                    Card(
                        modifier = Modifier
                            .width(240.dp)
                            .clickable { onDishSelect(dish) }
                            .testTag("special_dish_card_${dish.id}"),
                        colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = dish.imageUrl,
                                contentDescription = dish.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = dish.name,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = DeepBurgundy
                                )
                                Text(
                                    text = dish.ingredients,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "€${dish.price}",
                                        color = Terracotta,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.WineBar,
                                            contentDescription = null,
                                            tint = LuxuryGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Consult Somm",
                                            fontSize = 10.sp,
                                            color = LuxuryGold,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chef's Table Experience Showcase Banner
        item {
            ChefsTableBanner(onClick = onChefsTableClick)
        }

        // Gifting & Planning shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable { viewModel.selectTab(AppTab.PRIVATE_DINING) }
                        .testTag("home_planning_shortcut"),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(imageVector = Icons.Filled.MeetingRoom, contentDescription = null, tint = OliveGreen)
                        Column {
                            Text("Private Dining", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBurgundy)
                            Text("Banqueting & Weddings", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable { viewModel.selectTab(AppTab.GIFTING) }
                        .testTag("home_gifting_shortcut"),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(imageVector = Icons.Filled.CardGiftcard, contentDescription = null, tint = OliveGreen)
                        Column {
                            Text("Gifting Platform", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBurgundy)
                            Text("Experience packages", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocatorScreenView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showBookingWizard by remember { mutableStateOf(false) }
    var wizardRestaurant by remember { mutableStateOf<Restaurant?>(null) }

    // Booking Wizard form fields status
    var bookingDate by remember { mutableStateOf("June 20, 2026") }
    var bookingTime by remember { mutableStateOf("20:30") }
    var partySize by remember { mutableStateOf(2) }
    var seatingArea by remember { mutableStateOf("Indoor Salon") }
    var requests by remember { mutableStateOf("") }

    if (showBookingWizard && wizardRestaurant != null) {
        Dialog(onDismissRequest = { showBookingWizard = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxuryCream),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("booking_wizard_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "RESERVE TABLE",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DeepBurgundy,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "At " + wizardRestaurant!!.name,
                        fontSize = 13.sp,
                        color = SoftBronze,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    HorizontalDivider(color = LuxuryGold.copy(alpha = 0.3f))

                    // Date Input
                    Column {
                        Text("Date", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = bookingDate,
                            onValueChange = { bookingDate = it },
                            modifier = Modifier.fillMaxWidth().testTag("booking_date_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = SoftBronze
                            )
                        )
                    }

                    // Time Input
                    Column {
                        Text("Preferred Time", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = bookingTime,
                            onValueChange = { bookingTime = it },
                            modifier = Modifier.fillMaxWidth().testTag("booking_time_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = SoftBronze
                            )
                        )
                    }

                    // Party Size count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Guests count", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBurgundy)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (partySize > 1) partySize-- },
                                modifier = Modifier.testTag("btn_minus_party")
                            ) {
                                Icon(imageVector = Icons.Filled.Remove, contentDescription = "Minus", tint = DeepBurgundy)
                            }
                            Text(
                                text = partySize.toString(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { partySize++ },
                                modifier = Modifier.testTag("btn_plus_party")
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Plus", tint = DeepBurgundy)
                            }
                        }
                    }

                    // Seating Area toggle
                    Column {
                        Text("Seating Ambiance", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            wizardRestaurant!!.areas.forEach { area ->
                                val selected = seatingArea == area
                                InputChip(
                                    selected = selected,
                                    onClick = { seatingArea = area },
                                    label = { Text(area, fontSize = 11.sp) },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = DeepBurgundy,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("chip_area_$area")
                                )
                            }
                        }
                    }

                    // Special instructions
                    Column {
                        Text("V.I.P Special Requests", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = requests,
                            onValueChange = { requests = it },
                            placeholder = { Text("E.g. Table near live music, allergies...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().height(80.dp).testTag("booking_remarks_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepBurgundy,
                                unfocusedBorderColor = SoftBronze
                            )
                        )
                    }

                    // Submit & Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showBookingWizard = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBurgundy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Go Back")
                        }

                        Button(
                            onClick = {
                                viewModel.makeNewReservation(
                                    restaurantId = wizardRestaurant!!.id,
                                    restaurantName = wizardRestaurant!!.name,
                                    city = wizardRestaurant!!.city,
                                    date = bookingDate,
                                    time = bookingTime,
                                    partySize = partySize,
                                    seatingArea = seatingArea,
                                    specialRequests = requests
                                )
                                showBookingWizard = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.5f).testTag("confirm_booking_button")
                        ) {
                            Text("Confirm Booking")
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Nostre Residenze",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Interact with physical premium destinations and book real tables.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        items(StaticData.restaurants) { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restaurant_card_${res.id}"),
                colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        AsyncImage(
                            model = res.imageUrl,
                            contentDescription = res.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f))
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Room, contentDescription = null, tint = DeepBurgundy, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(res.city, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBurgundy)
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = res.name,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = DeepBurgundy
                        )
                        Text(
                            text = res.description,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, tint = OliveGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Today: ${res.hours}", fontSize = 11.sp, color = OliveGreen)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                            Icon(imageVector = Icons.Filled.PhoneInTalk, contentDescription = null, tint = OliveGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VIP Desk: ${res.phone}", fontSize = 11.sp, color = OliveGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    // Join waiting list flow simulation
                                    viewModel.makeWaitlistEnrollment(
                                        res.id, res.name, res.city, "June 20, 2026", "20:00", 2, "Terrace Garden"
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("waitlist_button_${res.id}"),
                                border = BorderStroke(1.dp, SoftBronze),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftBronze)
                            ) {
                                Text("Waitlist", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    wizardRestaurant = res
                                    showBookingWizard = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.5f).testTag("reserve_button_${res.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Book Table", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MenuScreenView(
    viewModel: MainViewModel,
    onConsultSommelier: (Dish) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Antipasti") }
    val cart by viewModel.cartItems.collectAsState(initial = emptyList())
    val tableModeActive by viewModel.isTableModeActive.collectAsState()

    val filteredDishes = StaticData.menuDishes.filter { it.category == selectedCategory }

    Column(modifier = modifier.fillMaxSize()) {
        // Upper banner introducing the editorial menu experience
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepBurgundy)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "IL MENU EDITORIALE",
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Artisanal ingredients & regional recipes",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // Table mode active banner trigger
                if (!tableModeActive) {
                    Button(
                        onClick = { viewModel.toggleTableMode(true, 18) },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("activate_table_mode_button")
                    ) {
                        Text("Active Table 18", fontSize = 10.sp, color = WarmNeroText, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OliveGreen)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Table 18 Active", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Category Ribbon
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(SoftIvoryCard)
                .padding(vertical = 10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val categories = listOf("Antipasti", "Pasta", "Pizza", "Secondi", "Dolci")
            items(categories) { cat ->
                val active = selectedCategory == cat
                Button(
                    onClick = { selectedCategory = cat },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) DeepBurgundy else Color.Transparent,
                        contentColor = if (active) Color.White else SoftBronze
                    ),
                    border = if (active) null else BorderStroke(1.dp, SoftBronze),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("menu_category_$cat")
                ) {
                    Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Main List of Dishes
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(filteredDishes) { dish ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dish_card_${dish.id}"),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                    border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Dish Editorial photo
                            AsyncImage(
                                model = dish.imageUrl,
                                contentDescription = dish.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dish.name,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepBurgundy
                                )
                                Text(
                                    text = "Origin: " + dish.originStory,
                                    fontSize = 11.sp,
                                    color = SoftBronze,
                                    fontFamily = FontFamily.Serif,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = dish.ingredients,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            style = LocalTextStyle.current.copy(lineHeight = 15.sp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "€${dish.price}",
                                fontWeight = FontWeight.Bold,
                                color = Terracotta,
                                fontSize = 15.sp
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { onConsultSommelier(dish) },
                                    border = BorderStroke(1.dp, LuxuryGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("somm_btn_${dish.id}")
                                ) {
                                    Icon(imageVector = Icons.Filled.WineBar, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pairing Wine", fontSize = 11.sp, color = LuxuryGold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.addDishToCart(dish, 1, isTableService = tableModeActive)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("add_to_cart_${dish.id}")
                                ) {
                                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // Persistent shopping cart / Active Dining Table order strip at bottom
        if (cart.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("persistent_cart_strip"),
                color = DeepBurgundy,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val totalCount = cart.sumOf { it.quantity }
                        val totalPrice = cart.sumOf { it.price * it.quantity }
                        Text(
                            text = "$totalCount items in cellar order",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Est. Total: €$totalPrice",
                            color = LuxuryGold,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.clearCartItems() }) {
                            Text("Clear", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.submitCurrentOrder()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("checkout_button")
                        ) {
                            Text("Place Order", color = WarmNeroText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SommelierPairingDialog(
    dish: Dish,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val recommendation by viewModel.sommelierRecommendation.collectAsState()
    val isLoading by viewModel.isLoadingSommelier.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalDarkBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("sommelier_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "HOUSE SOMMELIER",
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Aesthetic pairing for: ${dish.name}",
                    fontSize = 12.sp,
                    color = OffWhite,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )

                HorizontalDivider(color = LuxuryGold.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = LuxuryGold, modifier = Modifier.size(30.dp))
                    Text("Searching ancient wine cellars...", color = OffWhite, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    Text(
                        text = recommendation,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif,
                        color = OffWhite,
                        style = LocalTextStyle.current.copy(lineHeight = 18.sp),
                        textAlign = TextAlign.Justify
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    modifier = Modifier.fillMaxWidth().testTag("add_wine_to_order_confirm")
                ) {
                    Text("In Ordine, Grazie", color = WarmNeroText)
                }
            }
        }
    }
}

@Composable
fun WineCellarView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var customDishQuery by remember { mutableStateOf("") }
    val sommelierRec by viewModel.sommelierRecommendation.collectAsStateWithLifecycle()
    val isLoadingSomm by viewModel.isLoadingSommelier.collectAsStateWithLifecycle()
    val matchedDishName by viewModel.matchedDishName.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "La Cantina dei Vini",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Italian historic estate maps, vintages, and sommelier services.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        // Interactive Custom Wine pairing box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalDarkBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sommelier_intelligence_box"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DIGITAL SOMMELIER ENGINE",
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter any gourmet meal or preference (e.g. 'seared swordfish with capers') to receive an immediate Italian estate match.",
                        color = OffWhite,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customDishQuery,
                            onValueChange = { customDishQuery = it },
                            placeholder = { Text("What are you eating today?", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF2A2120),
                                unfocusedContainerColor = Color(0xFF2A2120),
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = SoftBronze
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sommelier_query_input")
                        )

                        Button(
                            onClick = {
                                if (customDishQuery.isNotBlank()) {
                                    val customDish = Dish(
                                        id = "custom",
                                        name = customDishQuery,
                                        category = "Custom",
                                        price = 0.0,
                                        ingredients = customDishQuery,
                                        originStory = "Custom",
                                        pairingWine = "Custom",
                                        imageUrl = ""
                                    )
                                    viewModel.requestSommelierPairing(customDish)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                            modifier = Modifier.testTag("sommelier_query_submit")
                        ) {
                            Text("Consult", color = WarmNeroText, fontSize = 12.sp)
                        }
                    }

                    if (matchedDishName.isNotBlank() && (sommelierRec.isNotBlank() || isLoadingSomm)) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = LuxuryGold.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ESTATE MATCH: $matchedDishName",
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isLoadingSomm) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = LuxuryGold, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Text(
                                text = sommelierRec,
                                fontSize = 12.sp,
                                color = OffWhite,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Exclusive Bottle Collections",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DeepBurgundy
            )
        }

        // List cellar wines
        items(StaticData.cellarWines) { wine ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wine_card_${wine.id}"),
                colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wine.name,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepBurgundy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = wine.vintage,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Terracotta,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Terracotta.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = wine.region + " • Grape: " + wine.grape + " • " + wine.style,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = wine.sommelierNotes,
                            fontSize = 11.sp,
                            color = WarmNeroText,
                            fontFamily = FontFamily.Serif,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("€${wine.priceGlass} / Glass", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBurgundy)
                                Text("€${wine.priceBottle} / Bottle", fontSize = 11.sp, color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    val dummyDish = Dish(wine.id, wine.name, "Wine", wine.priceBottle, wine.sommelierNotes, wine.grape, wine.name, "")
                                    viewModel.addDishToCart(dummyDish, 1, isTableService = false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("order_bottle_${wine.id}")
                            ) {
                                Text("Acquire", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ClubItaliaView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.customerProfile.collectAsState(initial = null)
    val reservations by viewModel.reservations.collectAsState(initial = emptyList())
    val tickets by viewModel.tickets.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Club Italia",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Your elite loyalty tier space, active table bookings, and masterclass invitations.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        // Luxury Black-Gold Digital Membership Card
        item {
            if (profile != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .testTag("club_membership_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1D1616), Color(0xFF382B29), Color(0xFF110B0B))
                                )
                            )
                            .border(BorderStroke(1.dp, LuxuryGold), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CLUB ITALIA",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = LuxuryGold,
                                    letterSpacing = 3.sp
                                )
                                Text(
                                    text = profile!!.memberLevel.uppercase() + " TIER",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = OffWhite,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LuxuryGold.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            // Center Account Details
                            Column {
                                Text(
                                    text = profile!!.customerName,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: " + profile!!.customerId + " • " + profile!!.email,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            // Card footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("LOYALTY CREDITS", fontSize = 9.sp, color = LuxuryGold, letterSpacing = 1.sp)
                                    Text(
                                        text = "${profile!!.loyaltyPoints} PTS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }

                                // Interactive QR representation
                                Box(
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White)
                                        .padding(4.dp)
                                ) {
                                    // Simulated minimal QR grid pattern
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(5) { rowIndex ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                repeat(5) { colIndex ->
                                                    val colored = (rowIndex + colIndex) % 2 == 0 || (rowIndex == 0 && colIndex == 0) || (rowIndex == 4 && colIndex == 4)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(if (colored) Color.Black else Color.White)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Points progression indicator
        item {
            if (profile != null) {
                val nextTierLimit = 3000
                val progress = profile!!.loyaltyPoints.toFloat() / nextTierLimit.toFloat()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Next Level: PLATINO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBurgundy)
                            Text("CREDIT Progress: ${profile!!.loyaltyPoints}/$nextTierLimit", fontSize = 11.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = DeepBurgundy,
                            trackColor = Color.LightGray.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        // Active/Upcoming reservations titles
        item {
            Text(
                text = "Your Dining Bookings",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DeepBurgundy
            )
        }

        if (reservations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No table bookings scheduled. Select 'Locations' to book our salons.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(reservations) { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_card_${booking.id}"),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                    border = BorderStroke(1.dp, if (booking.status == "Confirmed") DeepBurgundy.copy(alpha = 0.3f) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = booking.restaurantName,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DeepBurgundy
                            )
                            val statusTagColor = when (booking.status) {
                                "Confirmed" -> OliveGreen
                                "Waiting List" -> LuxuryGold
                                "Cancelled" -> Color.Gray
                                else -> DeepBurgundy
                            }
                            Text(
                                text = booking.status.uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusTagColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = booking.city + " • " + booking.bookingDate + " at " + booking.bookingTime,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (booking.specialRequests.isNotBlank()) {
                            Text(
                                text = "Guest Memo: " + booking.specialRequests,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (booking.status != "Cancelled" && booking.status != "Completed") {
                                OutlinedButton(
                                    onClick = { viewModel.cancelActiveReservation(booking) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
                                    border = BorderStroke(1.dp, Terracotta),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("cancel_reservation_btn_${booking.id}")
                                ) {
                                    Text("Cancel Reservation", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active ticketed masterclasses
        if (tickets.isNotEmpty()) {
            item {
                Text(
                    text = "Event Invitations & Masterclasses",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepBurgundy
                )
            }

            items(tickets) { ticket ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ticket_card_${ticket.id}"),
                    colors = CardDefaults.cardColors(containerColor = SoftIvoryCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ticket.eventTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepBurgundy)
                            Text("${ticket.ticketCount} Seats", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OliveGreen)
                        }
                        Text(ticket.location + " • " + ticket.date, fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Transaction: €${ticket.totalPaid}", fontSize = 11.sp, color = Terracotta, fontWeight = FontWeight.Bold)
                            Text("Access QR: " + ticket.qrCodeSeed.take(12), color = LuxuryGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ------ MAG COMPONENT (JOURNAL & EVENTS) ------

@Composable
fun JournalScreenView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tickets by viewModel.tickets.collectAsState(initial = emptyList())
    var selectedArticle by remember { mutableStateOf<LifestyleArticle?>(null) }

    if (selectedArticle != null) {
        Dialog(onDismissRequest = { selectedArticle = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LuxuryCream),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedArticle!!.category.uppercase(),
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                        IconButton(onClick = { selectedArticle = null }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = DeepBurgundy)
                        }
                    }

                    Text(
                        selectedArticle!!.title,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DeepBurgundy
                    )

                    Text(
                        "Written by " + selectedArticle!!.author + " on " + selectedArticle!!.date + " • " + selectedArticle!!.readTime,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AsyncImage(
                        model = selectedArticle!!.imageUrl,
                        contentDescription = selectedArticle!!.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = selectedArticle!!.content,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif,
                        style = LocalTextStyle.current.copy(lineHeight = 19.sp),
                        textAlign = TextAlign.Justify,
                        color = WarmNeroText
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "L'Eco d'Italia Journal",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "A luxury magazine diving into gastronomy, travel guides, and vineyard diaries.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        // Magazine layout
        items(StaticData.journalArticles) { article ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedArticle = article }
                    .testTag("journal_article_${article.id}"),
                colors = CardDefaults.cardColors(containerColor = SoftIvoryCard)
            ) {
                Column {
                    AsyncImage(
                        model = article.imageUrl,
                        contentDescription = article.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = article.category.uppercase(),
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = article.title,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepBurgundy,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = article.excerpt,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "By ${article.author}",
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = SoftBronze
                            )
                            Text(
                                text = "Read Article →",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepBurgundy
                            )
                        }
                    }
                }
            }
        }

        // Masterclasses scheduling area inside Journal tab
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Elite Clubhouse Masterclasses",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Secure exclusive tickets to high-vintage masterclasses & chef summits.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        items(StaticData.hospitalityEvents) { event ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_card_${event.id}"),
                colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.3f))
            ) {
                Column {
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = event.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = event.title,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DeepBurgundy
                        )
                        Text(
                            text = event.description,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Room, contentDescription = null, tint = OliveGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.location, fontSize = 11.sp, color = OliveGreen)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null, tint = OliveGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(event.date, fontSize = 11.sp, color = OliveGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("€${event.price} / Ticket", color = Terracotta, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Button(
                                onClick = {
                                    viewModel.purchaseEventTicket(event, 2)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("buy_ticket_${event.id}")
                            ) {
                                Text("Acquire 2 Tickets", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ------ PRIVATE DINING VIEW ------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateDiningView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val inquiries by viewModel.privateInquiries.collectAsStateWithLifecycle()

    var city by remember { mutableStateOf("Milan") }
    var restaurant by remember { mutableStateOf("Caffè Baglioni & Giardino") }
    var date by remember { mutableStateOf("October 24, 2026") }
    var guests by remember { mutableStateOf(24) }
    var notes by remember { mutableStateOf("") }
    var successAlert by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Private Dining Planner",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Bespoke salons for celebrations, weddings, and corporate hospitality.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                modifier = Modifier.fillMaxWidth().testTag("dining_planner_form")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Submit Event Inquiry", fontWeight = FontWeight.Bold, color = DeepBurgundy)

                    Column {
                        Text("Destination City", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text("Bespoke Residence", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = restaurant,
                            onValueChange = { restaurant = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Desired Date", fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estimated Guest Count", fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = guests.toString(),
                                onValueChange = { guests = it.toIntOrNull() ?: 10 },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Column {
                        Text("Catering style or custom requests", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Describe floral arches, sommelier cellaring bounds, live acoustic expectations...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.submitPrivateEventInquiry(city, restaurant, date, guests, notes)
                            successAlert = "Gratias. Our banquet director will formulate an exquisite plan."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        modifier = Modifier.fillMaxWidth().testTag("submit_private_dining_btn")
                    ) {
                        Text("File Banquet Proposal")
                    }

                    if (successAlert.isNotBlank()) {
                        Text(
                            text = successAlert,
                            color = OliveGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        if (inquiries.isNotEmpty()) {
            item {
                Text("Active Banquet Proposals", fontWeight = FontWeight.Bold, color = DeepBurgundy)
            }

            items(inquiries) { inq ->
                Card(colors = CardDefaults.cardColors(containerColor = SoftIvoryCard), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(14.dp)) {
                        Text(inq, fontSize = 12.sp, fontFamily = FontFamily.Serif, color = WarmNeroText)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ------ GIFTING PLATFORM SCREEN ------

@Composable
fun GiftingPlatformView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val gifts by viewModel.purchasedGiftCards.collectAsStateWithLifecycle()

    var amount by remember { mutableStateOf(100.0) }
    var recipient by remember { mutableStateOf("giulia@bellini-family.it") }
    var message by remember { mutableStateOf("Wishing you a sunset dinner in Florence with Love.") }
    var purchaseSuccess by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Luxury Gifting Suite",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = DeepBurgundy
                )
                Text(
                    text = "Present dining vouchers, vintage estate vintages, and bespoke hotel stays.",
                    fontSize = 12.sp,
                    color = SoftBronze
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftIvoryCard),
                modifier = Modifier.fillMaxWidth().testTag("gift_card_form")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select dining envelope amount", fontWeight = FontWeight.Bold, color = DeepBurgundy)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50.0, 100.0, 250.0, 500.0).forEach { amnt ->
                            val selected = amount == amnt
                            Button(
                                onClick = { amount = amnt },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) DeepBurgundy else LuxuryGold.copy(alpha = 0.2f),
                                    contentColor = if (selected) Color.White else DeepBurgundy
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("€${amnt.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column {
                        Text("Recipient Email Handle", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = recipient,
                            onValueChange = { recipient = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text("Bespoke Dedication Message", fontSize = 11.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            modifier = Modifier.fillMaxWidth().height(70.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.buyGiftCard(amount, recipient, message)
                            purchaseSuccess = "Envelope purchased! An authentic gold-foiled ticket dispatch has been queued to $recipient."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBurgundy),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_gifting_button")
                    ) {
                        Text("Acquire & Send Luxury Gift")
                    }

                    if (purchaseSuccess.isNotBlank()) {
                        Text(purchaseSuccess, color = OliveGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        if (gifts.isNotEmpty()) {
            item {
                Text("Your Active Gift Dispatch Ledger", fontWeight = FontWeight.Bold, color = DeepBurgundy)
            }

            items(gifts) { gift ->
                Card(colors = CardDefaults.cardColors(containerColor = SoftIvoryCard), modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Text(gift, fontSize = 12.sp, fontFamily = FontFamily.Serif, color = WarmNeroText)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ------ CHEF'S TABLE ATELIER EXPERIENCE COMPONENTS ------

@Composable
fun ChefsTableBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chefs_table_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalDarkBg) // Dark luxury theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "LA TAVOLA DELLO CHEF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuxuryGold,
                    letterSpacing = 2.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "The Michelin Tasting Table",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = LuxuryGold
            )
            
            Text(
                text = "Join Chef Marco for an intimate, theatrical 5-course modern Italian gastronomy sequence. Includes rare private-cellar reserve pairings curated by our master sommelier.",
                fontSize = 12.sp,
                color = OffWhite,
                modifier = Modifier.padding(vertical = 8.dp),
                lineHeight = 16.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "€240 / Host • Intimate Seating",
                    fontSize = 11.sp,
                    color = SoftBronze,
                    fontWeight = FontWeight.Medium
                )
                
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).testTag("reserve_chefs_table_banner_btn")
                ) {
                    Text(
                        text = "Reserve",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalDarkBg
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChefsTableBookingDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var selectedCity by remember { mutableStateOf("Milan") }
    var selectedPartySize by remember { mutableStateOf(2) }
    var selectedDate by remember { mutableStateOf("Friday, June 19") }
    var selectedTime by remember { mutableStateOf("19:30") }
    var addWineFlight by remember { mutableStateOf(true) }
    var dietaryNotes by remember { mutableStateOf("") }
    var bookingConfirmed by remember { mutableStateOf(false) }

    val basePrice = 240
    val winePrice = 110
    val totalPerPerson = basePrice + if (addWineFlight) winePrice else 0
    val totalCost = totalPerPerson * selectedPartySize

    Dialog(onDismissRequest = { if (!bookingConfirmed) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalDarkBg),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp)
                .testTag("chefs_table_booking_dialog")
        ) {
            if (bookingConfirmed) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "La Tavola dello Chef Reserved",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = LuxuryGold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your intimate theatrical tasting table for $selectedPartySize guests in $selectedCity on $selectedDate has been confirmed. You have been awarded +500 Club Italia VIP points!",
                        fontSize = 13.sp,
                        color = OffWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Accedi al Club Italia", color = CharcoalDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "LA TAVOLA DELLO CHEF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold,
                                letterSpacing = 3.sp
                            )
                            Text(
                                text = "Intimate Culinary Theatre",
                                fontSize = 11.sp,
                                color = SoftBronze
                            )
                        }
                    }

                    // Curated multi-course menu display
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF261D1C), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "THE DEUSTATION SEQUENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            val courses = listOf(
                                Triple("1. Aperitivo", "Ostrica e Franciacorta", "Rock oyster, wild herb granita, poured prosecco cream & Franciacorta 2013"),
                                Triple("2. Antipasto", "Crudo di Ricciola", "Yellowtail crudo, blood orange reductions, cold-pressed fennel, Vermentino 2021"),
                                Triple("3. Il Primo", "Tortelli di Zucca", "Handmade pumpkin tortelli, sage-infused brown butter, shaved winter truffles, Barbaresco 2018"),
                                Triple("4. Il Secondo", "Filetto al Barolo", "Braised beef tenderloin fillet, caramelized estate shallots, sunchoke puree, Sassicaia 2016"),
                                Triple("5. Il Dolce", "Sfera di Gianduja", "Gold-brushed hazelnut gianduja orb, warm mountain berries glass, Recioto Recito 2019")
                            )
                            
                            courses.forEach { (courseNum, dishName, harmony) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(courseNum, fontSize = 10.sp, color = SoftBronze, fontWeight = FontWeight.Bold)
                                        Text(dishName, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(harmony, fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }

                    // Selection parameters
                    item {
                        Text("Configure Tasting Reservation", fontWeight = FontWeight.Bold, color = LuxuryGold, fontSize = 13.sp)
                    }

                    // CITY SELECTOR
                    item {
                        Column {
                            Text("Piazza Location", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cities = listOf("Milan", "Rome", "Florence", "Venice")
                                cities.forEach { city ->
                                    val isSel = selectedCity == city
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) LuxuryGold else Color(0xFF2E2423),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedCity = city }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = city,
                                            fontSize = 11.sp,
                                            color = if (isSel) CharcoalDarkBg else Color.White,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PARTY SIZE SELECTOR
                    item {
                        Column {
                            Text("Atelier Seating (Intimate Cap)", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val capacities = listOf(2, 4, 6)
                                capacities.forEach { cap ->
                                    val isSel = selectedPartySize == cap
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) LuxuryGold else Color(0xFF2E2423),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedPartySize = cap }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$cap Guests",
                                            fontSize = 11.sp,
                                            color = if (isSel) CharcoalDarkBg else Color.White,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // DATE SELECTOR
                    item {
                        Column {
                            Text("Exclusive Date Slots", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val dates = listOf("Friday, June 19", "Saturday, June 20", "Sunday, June 21")
                                dates.forEach { dt ->
                                    val isSel = selectedDate == dt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) LuxuryGold else Color(0xFF2E2423),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedDate = dt }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dt.substringBefore(","),
                                            fontSize = 11.sp,
                                            color = if (isSel) CharcoalDarkBg else Color.White,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SITTING TIMING SELECTOR
                    item {
                        Column {
                            Text("Theatrical Show Sitting", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val timings = listOf("19:30", "21:00")
                                timings.forEach { tm ->
                                    val isSel = selectedTime == tm
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSel) LuxuryGold else Color(0xFF2E2423),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedTime = tm }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tm,
                                            fontSize = 11.sp,
                                            color = if (isSel) CharcoalDarkBg else Color.White,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PRESTIGE WINE PAIRING SWITCH
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF211716), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Prestige Sommelier Pairing", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text("Include rare library flight (+€110 per guest)", color = Color.Gray, fontSize = 10.sp)
                            }
                            Switch(
                                checked = addWineFlight,
                                onCheckedChange = { addWineFlight = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CharcoalDarkBg,
                                    checkedTrackColor = LuxuryGold,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color(0xFF2E2423)
                                )
                            )
                        }
                    }

                    // DIETARY NOTES Input
                    item {
                        Column {
                            Text("Dietary Requests / Notes", fontSize = 11.sp, color = SoftBronze, fontWeight = FontWeight.Medium)
                            OutlinedTextField(
                                value = dietaryNotes,
                                onValueChange = { dietaryNotes = it },
                                placeholder = { Text("Allergies, preferences, celebrations...", color = Color.Gray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().height(65.dp).padding(top = 4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = SoftBronze,
                                    focusedContainerColor = Color(0xFF231B1B),
                                    unfocusedContainerColor = Color(0xFF231B1B)
                                )
                            )
                        }
                    }

                    // BILL SUM AND CONFIRM ACTION
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tasting Sequence Total", fontSize = 11.sp, color = SoftBronze)
                                Text("€$totalCost", fontSize = 18.sp, color = LuxuryGold, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = {
                                    viewModel.bookChefsTable(
                                        city = selectedCity,
                                        date = selectedDate,
                                        time = selectedTime,
                                        partySize = selectedPartySize,
                                        includeWineFlight = addWineFlight,
                                        dietaryRestrictions = dietaryNotes
                                    )
                                    bookingConfirmed = true
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_chefs_table_booking_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Book Tasting & Earn +500 VP",
                                    color = CharcoalDarkBg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

