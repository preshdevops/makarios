package com.makarios.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Bible translation metadata.
 */
data class BibleVersion(
    val code: String,          // e.g. "web", "kjv", "bbe", "asv"
    val displayName: String,   // e.g. "WEB", "KJV", "BBE", "ASV"
    val fullTitle: String,     // e.g. "World English Bible"
    val isOffline: Boolean = false,
    val note: String           // e.g. "Built-in · Modern English", "Classic & Poetic"
)

/**
 * Repository for switching Bible translations and fetching verses in alternate versions.
 *
 * Uses the local 31,000+ verse corpus (World English Bible) for instant offline reads,
 * and fetches from the free public bible-api.com for KJV, BBE, and ASV with an in-memory cache.
 */
object BibleVersionRepository {

    val availableVersions: List<BibleVersion> = listOf(
        BibleVersion(
            code = "web",
            displayName = "WEB",
            fullTitle = "World English Bible",
            isOffline = true,
            note = "Built-in · Modern & Clear"
        ),
        BibleVersion(
            code = "kjv",
            displayName = "KJV",
            fullTitle = "King James Version",
            isOffline = false,
            note = "Classic & Poetic"
        ),
        BibleVersion(
            code = "bbe",
            displayName = "BBE",
            fullTitle = "Bible in Basic English",
            isOffline = false,
            note = "Simple & Accessible"
        ),
        BibleVersion(
            code = "asv",
            displayName = "ASV",
            fullTitle = "American Standard Version",
            isOffline = false,
            note = "Literal & Faithful"
        )
    )

    // Cache: "reference:versionCode" -> text
    private val textCache = mutableMapOf<String, String>()

    /**
     * Retrieves the text of a verse in the requested translation.
     *
     * @param reference   The Scripture citation, e.g. "John 3:16" or "Psalm 23:1"
     * @param fallbackText The local text already loaded from verses.json (WEB)
     * @param version     The chosen [BibleVersion]
     * @return The verse text in the requested version, or [fallbackText] on error or offline
     */
    suspend fun getVerseText(
        reference: String,
        fallbackText: String,
        version: BibleVersion
    ): String = withContext(Dispatchers.IO) {
        val cleanFallback = com.makarios.app.data.bible.ScriptureText.clean(fallbackText)
        if (version.code == "web" || version.isOffline) {
            return@withContext cleanFallback
        }

        val cacheKey = "${reference.trim().lowercase()}:${version.code}"
        textCache[cacheKey]?.let { return@withContext it }

        try {
            val encodedRef = URLEncoder.encode(reference.trim(), "UTF-8")
            val urlString = "https://bible-api.com/$encodedRef?translation=${version.code}"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("User-Agent", "MakariosApp/1.0")

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)
                val rawText = json.optString("text", "").trim()
                if (rawText.isNotBlank()) {
                    val cleaned = com.makarios.app.data.bible.ScriptureText.clean(rawText)
                    textCache[cacheKey] = cleaned
                    return@withContext cleaned
                }
            }
        } catch (_: Exception) {
            // Network failure or timeout - seamlessly return local text
        }

        cleanFallback
    }
}
