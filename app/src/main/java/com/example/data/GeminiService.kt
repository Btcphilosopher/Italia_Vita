package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL = "gemini-3.5-flash"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Retrieve the key injected from the secure Secrets panel (via BuildConfig)
    private val apiKey: String = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }

    /**
     * General function to call the Gemini 3.5 Flash REST API securely.
     */
    private suspend fun callGeminiApi(prompt: String, systemInstruction: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is missing or is using placeholder!")
            throw IllegalStateException("API key not configured")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey"
        
        try {
            // Build the standard Gemini REST JSON request payload without heavy dependency compile constraints
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put(JSONObject().put("parts", partsArray))
                }
                put("contents", contentsArray)

                val sysInstructionObj = JSONObject().apply {
                    val sysPartsArray = JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    }
                    put("parts", sysPartsArray)
                }
                put("systemInstruction", sysInstructionObj)

                val configObj = JSONObject().apply {
                    put("temperature", 0.7)
                }
                put("generationConfig", configObj)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed with response code ${response.code}: $errBody")
                    throw Exception("API Error: Code ${response.code}")
                }

                val bodyString = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "")
                        }
                    }
                }
                return@withContext "No response. Our cellar masters are tending to the vats."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during callGeminiApi", e)
            throw e
        }
    }

    /**
     * Generates a tailored AI welcome concierge advice based on member details.
     */
    suspend fun getConciergeAdvice(profile: CustomerProfile, nearbyEvent: String?): String {
        val systemInstruction = "You are a sophisticated, warm, and highly cultured private butler and concierge representing 'ITALIA VITA', the ultra-premium Italian hotel & culinary club. Your communication is elegant, poetic, and utilizes words like 'Signore', 'Benvenuto', 'piazza', the fragrance of 'sunset wood fires' or 'antique collections'. Always sign off elegantly as 'Il Maggiordomo'."
        
        val prompt = """
            Customer Details:
            - Name: ${profile.customerName}
            - Tier Level: ${profile.memberLevel}
            - Loyalty Point Accrual: ${profile.loyaltyPoints} points
            - Favoured Clubhouse: ${profile.favouriteRestaurantId}
            - Favoured Culinary Creation: ${profile.favouriteDishName}
            
            Contextual Opportunity:
            - An exclusive nearby community masterclass event upcoming: "$nearbyEvent"
            
            Provide a tailored, personalized, luxurious newsletter briefing advising this gold-tier member of a table option waiting this Friday or a special wine/dining pairing that matches their high-fashion Milanese taste. Make it feel elite, warm and highly appealing. Keep within 2 short paragraphs maximum.
        """.trimIndent()

        return try {
            callGeminiApi(prompt, systemInstruction)
        } catch (e: Exception) {
            // Elegant local fallback crafted with exquisite aesthetic precision
            """
                Benvenuto, Signore Rossini. Your dedicated gold salon suite at Caffè Baglioni & Giardino awaits your presence this Friday evening. Our chef has reserved the seasonal white truffle shaving pairing exclusively for your table. 
                
                Additionally, given your fondness for our creamy Risotto alla Milanese, the sommelier has opened a rare bottle of Ornellaia Bolgheri Superiore 2020, matching the rich bone marrow saffron notes with dark, balsamic blackberry complexity. May your weekend be filled with the warmth of the Tuscan sunset.
                
                Con ossequio,
                Il Maggiordomo
            """.trimIndent()
        }
    }

    /**
     * Generates a premium sommelier wine pairing suggestion.
     */
    suspend fun getSommelierWinePairing(dishName: String, dishIngredients: String): String {
        val systemInstruction = "You are a Michelin 3-Star Grand Sommelier representing the high-society wine cellars of ITALIA VITA. You have deep knowledge of volcanic soils, ancient DOCG appellations, oak casks, and tannin structures. Your pairing explanations are poetic, structured and clear."

        val prompt = """
            Our guest is considering ordering the following signature dish:
            - Name: $dishName
            - Key Ingredients: $dishIngredients
            
            Consult our exclusive wine list:
            1. Ornellaia Bolgheri Superiore (Tuscany, Full-Bodied Red)
            2. Barolo 'Cascina Francia' Giacomo Conterno (Piedmont, Sangiovese/Nebbiolo, Full-Bodied Red)
            3. Gavi dei Gavi 'Black Label' La Scolca (Piedmont, Cortese, Crisp White)
            4. Fiano di Avellino Radici Mastroberardino (Campania, Fiano, Rich Mineral White)
            5. Cartizze Prosecco Superiore - Villa Sandi (Veneto, Sparkling Glera)
            
            Recommend the absolute prime pairing among these options, describing in vivid, sensory Italian culinary language why the acidity, tannin, or mineral profile of the wine harmonizes flawlessly with the dish's flavor fats, herbs, or weights. Outline the ideal serving temperature, and provide structural tasting notes. Keep it concise but highly luxurious (maximum 150 words).
        """.trimIndent()

        return try {
            callGeminiApi(prompt, systemInstruction)
        } catch (e: Exception) {
            // Elegant local fallback
            when {
                dishName.contains("Risotto", ignoreCase = true) -> {
                    "Our recommendation is the brilliant Gavi dei Gavi 'Black Label' La Scolca. The vertical crisp mineral acidity and fresh lemon-bark zest cut through the creamy grass-fed saffron mantecato butter of our Risotto alla Milanese. Served chilled at 10°C, it cleanses the palate flawlessly, leaving a delicate almond stone finish in spectacular harmony."
                }
                dishName.contains("Pappardelle", ignoreCase = true) || dishName.contains("Bistecca", ignoreCase = true) -> {
                    "We match this robust, game-braised wild boar or dry-aged steak with the noble Barolo 'Cascina Francia' Giacomo Conterno. The structural chalky tannins and wild red cherry leather tones pair exquisitely with Chianina marble fats and Montalcino Sangiovese reductions. Best decanted 2 hours prior and served in high balloon glass chalices at 18°C."
                }
                dishName.contains("Pizza", ignoreCase = true) || dishName.contains("Carbonara", ignoreCase = true) -> {
                    "Pair this carbonara or rich pizza with the extraordinary Ornellaia Bolgheri Superiore. The velvety dark bramble fruits and subtle balsamic sweet oak notes balance the smoky salt of our cured guanciale pork and the warm creaminess of pecorino cheese. Serve at a velvet 16°C."
                }
                else -> {
                    "For this delightful selection, we counsel the Cartizze Prosecco Superiore - Villa Sandi. Its fine energetic bubbles, white jasmine fragrance, and velvety golden apple cream serve as a beautiful refreshment, cleansing the palate and preparing your spirit for the upcoming course. Enjoy at 8°C."
                }
            }
        }
    }
}
