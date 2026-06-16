package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// --- ROOM DATABASE ENTITIES ---

@Entity(tableName = "reservations")
data class Reservation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val restaurantId: String,
    val restaurantName: String,
    val city: String,
    val bookingDate: String, // e.g., "June 20, 2026"
    val bookingTime: String, // e.g., "20:30"
    val partySize: Int,
    val seatingArea: String, // "Indoor", "Terrace Garden", "Chef's Table", "Private Suite"
    val specialRequests: String = "",
    val status: String = "Confirmed", // "Confirmed", "Waiting List", "Completed", "Cancelled"
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "customer_profile")
data class CustomerProfile(
    @PrimaryKey val customerId: String = "vita_member_501",
    val customerName: String = "Tommaso Rossini",
    val memberLevel: String = "Oro", // "Azzurro", "Oro", "Platino"
    val loyaltyPoints: Int = 2450,
    val favouriteRestaurantId: String = "baglioni_milan",
    val favouriteDishName: String = "Risotto alla Milanese",
    val email: String = "tom@ahyx.org"
) : Serializable

@Entity(tableName = "table_order_items")
data class TableOrderItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dishName: String,
    val category: String,
    val price: Double,
    val quantity: Int,
    val isTableService: Boolean = true, // true if seated table order, false if delivery/takeaway
    val orderTime: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "masterclass_tickets")
data class EventTicket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventId: String,
    val eventTitle: String,
    val location: String,
    val date: String,
    val ticketCount: Int,
    val totalPaid: Double,
    val qrCodeSeed: String = "TICKET-${System.currentTimeMillis()}"
) : Serializable

// --- READ-ONLY DOMAIN MODELS AND STATIC SEED DATA ---

data class Restaurant(
    val id: String,
    val name: String,
    val city: String,
    val description: String,
    val address: String,
    val hours: String,
    val phone: String,
    val imageUrl: String,
    val rating: Double = 4.9,
    val areas: List<String> = listOf("Indoor Salon", "Terrace Garden", "Sommelier Cellar", "Private Dining Room")
)

data class Dish(
    val id: String,
    val name: String,
    val category: String, // "Antipasti", "Pasta", "Pizza", "Secondi", "Dolci"
    val price: Double,
    val ingredients: String,
    val originStory: String,
    val pairingWine: String,
    val imageUrl: String,
    val preparationTimeMin: Int = 20,
    val allergens: String = "None"
)

data class Wine(
    val id: String,
    val name: String,
    val region: String, // "Tuscany", "Piedmont", "Veneto", "Sicily", "Abruzzo"
    val vintage: String,
    val grape: String,
    val style: String, // "Crisp White", "Full-Bodied Red", "Sparkling Prosecco", "Sweet Passito"
    val priceGlass: Double,
    val priceBottle: Double,
    val sommelierNotes: String,
    val rating: Double = 4.8,
    val imageUrl: String
)

data class LifestyleArticle(
    val id: String,
    val title: String,
    val category: String, // "Gastronomy", "Design", "Travel", "Sommelier Diaries"
    val readTime: String,
    val excerpt: String,
    val content: String,
    val author: String,
    val date: String,
    val imageUrl: String
)

data class HospitalityEvent(
    val id: String,
    val title: String,
    val location: String,
    val date: String,
    val price: Double,
    val description: String,
    val availableSeats: Int,
    val imageUrl: String
)

object StaticData {
    val restaurants = listOf(
        Restaurant(
            id = "baglioni_milan",
            name = "Caffè Baglioni & Giardino",
            city = "Milan",
            description = "High-fashion, sophisticated Milanese dining nestled near Via Monte Napoleone. Combining luxury modern Italian craft with a secret atmospheric courtyard garden.",
            address = "Via Senato 5, 20121 Milano MI",
            hours = "12:00 – 23:30",
            phone = "+39 02 7700 3000",
            imageUrl = "https://images.unsplash.com/photo-1520175480921-4edfa2983e0f"
        ),
        Restaurant(
            id = "palagio_florence",
            name = "Il Palagio di Firenze",
            city = "Florence",
            description = "Elegance under high vaulted ceilings and manicured Tuscan cypress gardens. Savour legendary regional hand-rolled pasta, wild boar sauces, and premium Chianti.",
            address = "Borgo Pinti 99, 50121 Firenze FI",
            hours = "12:30 – 23:00",
            phone = "+39 055 26261",
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947"
        ),
        Restaurant(
            id = "quadri_venice",
            name = "Ristorante Quadri Venezia",
            city = "Venice",
            description = "An editorial culinary experience in a grand 18th-century salon directly overlooking Saint Mark's Square. Specializes in lagoon seafood and Adriatic catches.",
            address = "Piazza San Marco 121, 30124 Venezia VE",
            hours = "12:00 – 15:00, 19:00 – 22:30",
            phone = "+39 041 522 2105",
            imageUrl = "https://images.unsplash.com/photo-1534080391025-09795d197360"
        ),
        Restaurant(
            id = "olimpia_rome",
            name = "Osteria Donna Olimpia",
            city = "Rome",
            description = "Tucked behind a quiet Roman piazza, Donna Olimpia serves Roman classics prepared with rigorous organic craftsmanship: Carbonara, Amatriciana, and wood-fired Pizza.",
            address = "Via di Donna Olimpia 82, 00152 Roma RM",
            hours = "12:00 – 23:00",
            phone = "+39 06 581 1234",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591"
        ),
        Restaurant(
            id = "villadeste_lakecomo",
            name = "Terrazza d'Este",
            city = "Lake Como",
            description = "Premium dining suspended directly over Lake Como's sparkling shoreline. A sanctuary of luxury where chef-crafted sea bass pairings and sparkling Franciacorta rule the night.",
            address = "Via Regina 40, 22012 Cernobbio CO",
            hours = "12:30 – 14:30, 19:30 – 22:30",
            phone = "+39 031 3481",
            imageUrl = "https://images.unsplash.com/photo-1531973576160-7125cd663d86"
        )
    )

    val menuDishes = listOf(
        // Antipasti
        Dish(
            id = "antipasto_crudo",
            name = "Carpaccio San Daniele con Fichi",
            category = "Antipasti",
            price = 24.0,
            ingredients = "24-Month aged Prosciutto di San Daniele, sweet local black figs, micro arugula, 12-year traditional balsamic glaze, virgin Tuscan olive oil.",
            originStory = "Developed in Friuli-Venezia Giulia, combining the extreme sweetness of hand-selected summer black figs with the salt-cured depth of authentic San Daniele pork legs.",
            pairingWine = "Valdobbiadene Prosecco Superiore DOCG",
            imageUrl = "https://images.unsplash.com/photo-1534080391025-09795d197360"
        ),
        Dish(
            id = "antipasto_fiori",
            name = "Fiori di Zucca Ripieni",
            category = "Antipasti",
            price = 19.5,
            ingredients = "Delicate organic squash blossoms, stuffed with soft Roman ricotta cheese and wild mint, tempura crisp battered, served over saffron emulsion.",
            originStory = "An elegant interpretation of Trastevere street food, lifted with premium Amalfi lemon zest to brighten the warm, creamy herbal center.",
            pairingWine = "Vermentino di Gallura DOCG",
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947"
        ),
        // Pasta
        Dish(
            id = "pasta_risotto",
            name = "Risotto Zafferano e Midollo alla Milanese",
            category = "Pasta",
            price = 32.0,
            ingredients = "Superfine Vialone Nano rice, Aquila saffron thread steep, rich homemade bone stock, grass-fed butter pan-mantecato, roasted veal marrow crown.",
            originStory = "Originally documented in Milan's historic documents from 1574. Crafted sequentially with extreme slow-stirring to unleash pure dynamic starch creaminess.",
            pairingWine = "Barbera d'Asti DOCG",
            imageUrl = "https://images.unsplash.com/photo-1612874742237-6526221588e3"
        ),
        Dish(
            id = "pasta_wildboar",
            name = "Pappardelle al Cinghiale di Montalcino",
            category = "Pasta",
            price = 28.5,
            ingredients = "Fresh house-rolled 30-egg-yolk flat pappardelle ribbons, hand-pulled wild boar ragù slow-braised for 14 hours in Sangiovese, juniper berries, bay leaves.",
            originStory = "A staple of rustic Tuscan aristocratic estates around Siena, representing the balance of wild game strength and luxurious tender pasta sheets.",
            pairingWine = "Brunello di Montalcino DOCG",
            imageUrl = "https://images.unsplash.com/photo-1612874742237-6526221588e3"
        ),
        Dish(
            id = "pasta_carbonara",
            name = "La Carbonara Romana di Donna Olimpia",
            category = "Pasta",
            price = 26.0,
            ingredients = "Rummo bronze-die Mezze Maniche, crispy cured pigs cheek (guanciale di Amatrice), emulsified paste of Pecorino Romano DOC, dark black peppercorns, organic egg yolks.",
            originStory = "No cream, no garlic. Roman golden standard. Born in the Appennines from herding coal workers, refined in modern Rome into a glossy, velvety rich cream.",
            pairingWine = "Frascati Superiore Riserva DOCG",
            imageUrl = "https://images.unsplash.com/photo-1612874742237-6526221588e3"
        ),
        // Pizza
        Dish(
            id = "pizza_margherita",
            name = "Pizza Regina Margherita",
            category = "Pizza",
            price = 22.0,
            ingredients = "Double fermented type 00 flour dough, sweet crushed San Marzano D.O.P tomatoes, fresh Buffalo Mozzarella di Campania, hand-torn basil, green olive oil drizzle.",
            originStory = "Created by chef Raffaele Esposito in 1889 to represent the flag of Italy. Wood-fired at 480°C for exactly 90 seconds to build the iconic leopard-spotted aerated crust.",
            pairingWine = "Etna Rosato DOC",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591"
        ),
        Dish(
            id = "pizza_tartufo",
            name = "Pizza Bianca di Noia al Tartufo Nero",
            category = "Pizza",
            price = 38.0,
            ingredients = "Aerated white pizza, sweet fior di latte base, shaved thin black summer truffles from Norcia, porcini cream, single origin olive oil.",
            originStory = "The scent of Umbrian autumn woods baked onto a light-as-air Roman pinsa crust. Highly aromatic, sophisticated and luxurious.",
            pairingWine = "Amarone della Valpolicella DOCG",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591"
        ),
        // Secondi
        Dish(
            id = "secondi_bistecca",
            name = "Bistecca alla Fiorentina (dry-aged)",
            category = "Secondi",
            price = 68.0,
            ingredients = "Chianina beef T-bone steak (800g), dry-aged for 45 days, seared over oak wood embers, coarse Maldon sea salt, rosemary cluster wood-basted.",
            originStory = "Derived from the ancient Florentine feast of Saint Lawrence, where open fires roasted incredible steaks for the town merchants.",
            pairingWine = "Sassicaia Bolgheri Sassicaia DOC",
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947"
        ),
        Dish(
            id = "secondi_spigola",
            name = "Spigola all'Acqua Pazza",
            category = "Secondi",
            price = 45.0,
            ingredients = "Line-caught Mediterranean sea bass fillet, poached in a 'crazy water' broth of sweet piennolo cherry tomatoes, green olives, capers, garlic, dry white Fiano wine.",
            originStory = "An old Neapolitan fisherman style where sea bass was cooked in seawater with wild tomatoes to balance the natural ocean salinity with high acidity.",
            pairingWine = "Fiano di Avellino DOCG",
            imageUrl = "https://images.unsplash.com/photo-1534080391025-09795d197360"
        ),
        // Dolci
        Dish(
            id = "dolci_tiramisu",
            name = "Tiramisù de la Nonna Caterina",
            category = "Dolci",
            price = 14.0,
            ingredients = "Sardinian ladyfingers submerged in dark volcanic Sicilian espresso, whipped sweet mascarpone layers, premium dark cocoa powder dusting, Marsala wine.",
            originStory = "Invented in Treviso, Veneto. Literally translating to 'pull me up' or 'wake me up' for its robust caffeine-and-sugar espresso fuel.",
            pairingWine = "Marsala Superiore Dolce DOC",
            imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9"
        ),
        Dish(
            id = "dolci_cannolo",
            name = "Gran Cannolo Palermo",
            category = "Dolci",
            price = 13.0,
            ingredients = "Fried crispy pastry pastry cylinder, filled with sweet sheep's milk ricotta cream, candied orange peel hearts, crushed bronte pistachios.",
            originStory = "Originating in the Caltanissetta region of Arab-influenced Sicily. Historically prepared during Carnival season as a symbol of hospitality and fertile abundance.",
            pairingWine = "Passito di Pantelleria DOC",
            imageUrl = "https://images.unsplash.com/photo-1571877227200-a0d98ea607e9"
        )
    )

    val cellarWines = listOf(
        Wine(
            id = "ornellaia",
            name = "Ornellaia Bolgheri Superiore",
            region = "Tuscany",
            vintage = "2020",
            grape = "Cabernet Sauvignon, Merlot",
            style = "Full-Bodied Red",
            priceGlass = 38.0,
            priceBottle = 195.0,
            sommelierNotes = "Intense, dark ruby color. A dense bouquet of dark berries, sweet spice, and roasted leather. Silky tannins frame a majestic, opulent finish of endless balsamic layers.",
            rating = 4.9,
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        ),
        Wine(
            id = "barolo_conterno",
            name = "Barolo 'Cascina Francia' Giacomo Conterno",
            region = "Piedmont",
            vintage = "2018",
            grape = "Nebbiolo",
            style = "Full-Bodied Red",
            priceGlass = 45.0,
            priceBottle = 260.0,
            sommelierNotes = "The pinnacle of Nebbiolo. Rose petals, damp forest earth, tar, and wild red cherries. High-tension acidity and structural chalky tannins provide legendary life.",
            rating = 5.0,
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        ),
        Wine(
            id = "gavi_la_scolca",
            name = "Gavi dei Gavi 'Black Label' La Scolca",
            region = "Piedmont",
            vintage = "2022",
            grape = "Cortese",
            style = "Crisp White",
            priceGlass = 16.0,
            priceBottle = 72.0,
            sommelierNotes = "Incredibly crisp. Flint minerals, cold stone, crisp green apples, lemon tree skin, and a salty, energetic, dry almond finish that pairing perfectly with seafood.",
            rating = 4.7,
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        ),
        Wine(
            id = "fiano_mastroberardino",
            name = "Fiano di Avellino Radici Mastroberardino",
            region = "Campania",
            vintage = "2021",
            grape = "Fiano",
            style = "Crisp White",
            priceGlass = 14.0,
            priceBottle = 58.0,
            sommelierNotes = "Deep golden-straw. Aromas of hazelnut skin, fresh wildflowers, pear nectar, and a smoky obsidian mineral back. Full-textured and sophisticated.",
            rating = 4.8,
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        ),
        Wine(
            id = "prosecco_cartizze",
            name = "Cartizze Prosecco Superiore DOCG - Villa Sandi",
            region = "Veneto",
            vintage = "NV",
            grape = "Glera",
            style = "Sparkling Prosecco",
            priceGlass = 18.0,
            priceBottle = 85.0,
            sommelierNotes = "From the golden steep hills of Cartizze. Intense fragrance of ripe golden apples, pear, and jasmine. Delicate, highly fine bubbles with a brilliant velvet finish.",
            rating = 4.9,
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        )
    )

    val journalArticles = listOf(
        LifestyleArticle(
            id = "art_truffle",
            title = "The Hunt for White Pearls in Piedmont",
            category = "Gastronomy",
            readTime = "6 min read",
            excerpt = "Under autumn mists, lagotto dogs track down the mythical white truffle of Alba. Discover the heritage, stories, and sensory secrets of Piedmont's underground gold.",
            author = "Lorenzo de' Medici",
            date = "Oct 12, 2025",
            content = "The cold morning air of the Langhe hills is heavy with fog. We follow Carlo and his faithful Lagotto Romagnolo dog, Rocco, into the damp chestnut forests of Alba. Rocco scratches the earth gently, revealing a pale, mud-caked tuber that smells intensely of honey, garlic, and wet earth. This is the Alba white truffle—the most expensive culinary treasure on Earth.\n\nWhite truffles grow symbiotically with oak, hazelnut, and poplar trees. Unlike black truffles, they can never be cultivated; they exist purely on nature's whim. To serve them properly, they are never cooked. Instead, they are shaved paper-thin directly over warm, rich butter tajarin pasta or fried organic duck eggs, allowing the raw friction of the heat to diffuse their intoxicating aroma. At ITALIA VITA, we fly these white beauties twice a week directly from Piedmont during truffle harvest season.",
            imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947"
        ),
        LifestyleArticle(
            id = "art_fermentation",
            title = "A Milanese Love Story: The Art of Slow Dough",
            category = "Gastronomy",
            readTime = "5 min read",
            excerpt = "Our master pizzaiolo breaks down why a 72-hour fermentation is non-negotiable for true digital luxury in contemporary baking craft.",
            author = "Chef Marco Vitali",
            date = "Nov 15, 2025",
            content = "Bread and dough are alive. At ITALIA VITA, our pizza base relies on a poolish sourdough starter that has been fed daily for nine years. Our flour is imported from Molino Pasini in Mantua—an ultra-fine Type 00 cold-ground on ancient stone mills.\n\nThe real secret, however, is patience. We ferment our dough under tight temperature-controlled cells for exactly 72 hours. Why? This allows complex lactic acid bacteria to completely break down complex starches into natural sugars. When fired inside our volcanic clay oven at 480°C, the moisture water-pockets vaporize instantly, leaving a signature aerated 'cornicione' that is incredibly crisp, light as air, and fully digestive.",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591"
        ),
        LifestyleArticle(
            id = "art_chianti",
            title = "The Hills of Sangiovese: Exploring Tuscany",
            category = "Sommelier Diaries",
            readTime = "8 min read",
            excerpt = "A curated sensory map detailing the historic sub-zones of Chianti Classico, from the rocky soils of Gaiole to the clay of Castellina.",
            author = "Somm. Isabella Rossi",
            date = "Jan 30, 2026",
            content = "To understand Tuscan red wine is to master the temperamental grape known as Sangiovese. Its name translates to the 'Blood of Jupiter'. In Chianti Classico, this native vine expresses its maximum potential, shifting from high-acid sour cherry to earthy tobacco, graphite, and leather flavors.\n\nDepending on where you stand relative to the ancient league of Chianti towns, the soils differ dramatically. In Gaiole, stone-rich 'Alberese' limestone provides lean, high-toned acidities. In Castellina, softer clay-shale 'Galestro' builds fleshy, broad, and ripe red fruit bodies. At ITALIA VITA, we offer tasting flights containing single-vineyard Cru selections from each town to guide your spirit directly into the heart of Tuscan geology.",
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        )
    )

    val hospitalityEvents = listOf(
        HospitalityEvent(
            id = "ev_somm",
            title = "Piedmont vs Tuscany: The Great Red Duel",
            location = "Milan Courtyard & Online",
            date = "Friday, June 26 at 19:30",
            price = 120.0,
            description = "A rare sommelier battle comparing five high-vintage Barolos against five iconic Brunellos of Montalcino. Accompanied by dry-aged Chianina slow cuts and white truffle appetizers.",
            availableSeats = 12,
            imageUrl = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3"
        ),
        HospitalityEvent(
            id = "ev_pasta",
            title = "Sfoglia Pasta Masterclass: 30-Egg Yolk Secrets",
            location = "Florence Il Palagio Kitchen",
            date = "Saturday, July 4 at 11:00",
            price = 85.0,
            description = "Learn the artisanal master craftsman methods of rolling paper-thin fresh egg pasta (sfoglia) by hand with rolling pin. Master classic Tuscan filling shapes like tortelli and culinary ragù.",
            availableSeats = 8,
            imageUrl = "https://images.unsplash.com/photo-1612874742237-6526221588e3"
        ),
        HospitalityEvent(
            id = "ev_music",
            title = "Venetian Sunset Jazz & Oyster Gala",
            location = "Ristorante Quadri Terrace",
            date = "Wednesday, July 15 at 18:30",
            price = 150.0,
            description = "A sophisticated, high-society evening on the historical St. Mark terrace features vintage jazz quartets, raw Adriatic oyster bars, and unlimited flows of luxury Bellini cocktails.",
            availableSeats = 25,
            imageUrl = "https://images.unsplash.com/photo-1534080391025-09795d197360"
        )
    )
}
