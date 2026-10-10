package com.makarios.app.data.bible

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class Testament {
    OT, NT
}

data class BibleBook(
    val name: String,
    val testament: Testament,
    val chapterCount: Int
)

data class BibleVerse(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val reference: String
)

data class VerseSelection(
    val book: String,
    val chapter: Int,
    val fromVerse: Int,
    val toVerse: Int,
    val text: String,
    val reference: String,
    val translation: String = "WEB"
)

object BibleRepository {

    private const val TAG = "BibleRepository"
    private val initMutex = Mutex()
    private var isInitialized = false

    private val booksList = mutableListOf<BibleBook>()
    // book -> chapter -> List<BibleVerse>
    private val chapterMap = HashMap<String, MutableMap<Int, MutableList<BibleVerse>>>()
    // normalized reference -> BibleVerse
    private val verseMap = HashMap<String, BibleVerse>()
    // all cleaned verses for full-text search
    private val allVerses = mutableListOf<BibleVerse>()

    val books: List<BibleBook>
        get() = booksList

    /**
     * Initializes the in-memory Bible index from assets/verses.json.
     * Loads once on Dispatchers.IO and stays in memory for instant (<50ms) access.
     */
    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        initMutex.withLock {
            if (isInitialized) return@withLock
            val start = System.currentTimeMillis()
            runCatching {
                val jsonString = context.assets.open("verses.json").bufferedReader().use { it.readText() }
                val array = JSONArray(jsonString)

                val bookMaxChapters = HashMap<String, Int>()

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val rawRef = obj.getString("r")
                    val rawText = obj.getString("t")

                    val normRef = ScriptureText.normalizeReference(rawRef)
                    val cleanText = ScriptureText.clean(rawText)

                    // Reference format: "Book Name Ch:v"
                    val lastSpace = normRef.lastIndexOf(' ')
                    if (lastSpace <= 0) continue
                    val bookName = normRef.substring(0, lastSpace)
                    val colonPart = normRef.substring(lastSpace + 1)
                    val colonIdx = colonPart.indexOf(':')
                    if (colonIdx <= 0) continue

                    val chapter = colonPart.substring(0, colonIdx).toIntOrNull() ?: 1
                    val verse = colonPart.substring(colonIdx + 1).toIntOrNull() ?: 1

                    val bVerse = BibleVerse(
                        book = bookName,
                        chapter = chapter,
                        verse = verse,
                        text = cleanText,
                        reference = normRef
                    )

                    allVerses.add(bVerse)
                    verseMap[normRef.lowercase()] = bVerse

                    val chapters = chapterMap.getOrPut(bookName) { HashMap() }
                    chapters.getOrPut(chapter) { mutableListOf() }.add(bVerse)

                    val maxCh = bookMaxChapters[bookName] ?: 0
                    if (chapter > maxCh) {
                        bookMaxChapters[bookName] = chapter
                    }
                }

                // Populate canonical book list
                val canonicalOrder = BibleReferenceParser.CANONICAL_BOOKS
                booksList.clear()
                canonicalOrder.forEachIndexed { index, name ->
                    val chCount = bookMaxChapters[name] ?: 1
                    val testament = if (index < 39) Testament.OT else Testament.NT
                    booksList.add(BibleBook(name, testament, chCount))
                }

                isInitialized = true
                Log.d(TAG, "Loaded ${allVerses.size} verses across ${booksList.size} books in ${System.currentTimeMillis() - start}ms")
            }.onFailure {
                Log.e(TAG, "Failed to load verses.json", it)
            }
        }
    }

    suspend fun getChapter(context: Context, book: String, chapter: Int): List<BibleVerse> {
        if (!isInitialized) initialize(context)
        val canonical = BibleReferenceParser.resolveBookName(book) ?: book
        return chapterMap[canonical]?.get(chapter) ?: emptyList()
    }

    suspend fun getVerse(context: Context, reference: String): BibleVerse? {
        if (!isInitialized) initialize(context)
        val parsed = BibleReferenceParser.parse(reference)
        if (parsed != null && parsed.fromVerse != null) {
            val key = "${parsed.book} ${parsed.chapter}:${parsed.fromVerse}".lowercase()
            return verseMap[key]
        }
        val cleanKey = ScriptureText.normalizeReference(reference).lowercase()
        return verseMap[cleanKey]
    }

    suspend fun getRange(
        context: Context,
        book: String,
        chapter: Int,
        fromVerse: Int,
        toVerse: Int
    ): List<BibleVerse> {
        val list = getChapter(context, book, chapter)
        val minV = minOf(fromVerse, toVerse)
        val maxV = maxOf(fromVerse, toVerse)
        // Cap at 8 verses
        val cappedMax = minOf(maxV, minV + 7)
        return list.filter { it.verse in minV..cappedMax }
    }

    suspend fun searchVerses(context: Context, query: String, limit: Int = 20): List<BibleVerse> {
        if (!isInitialized) initialize(context)
        val q = query.trim().lowercase()
        if (q.length < 2) return emptyList()

        return withContext(Dispatchers.Default) {
            val wordRegex = Regex("\\b${Regex.escape(q)}\\b", RegexOption.IGNORE_CASE)
            val exactMatches = mutableListOf<BibleVerse>()
            val substringMatches = mutableListOf<BibleVerse>()

            for (verse in allVerses) {
                if (wordRegex.containsMatchIn(verse.text)) {
                    exactMatches.add(verse)
                    if (exactMatches.size >= limit) break
                } else if (verse.text.contains(q, ignoreCase = true)) {
                    if (substringMatches.size < limit) {
                        substringMatches.add(verse)
                    }
                }
            }

            if (exactMatches.size >= limit) {
                exactMatches
            } else {
                (exactMatches + substringMatches).distinctBy { it.reference }.take(limit)
            }
        }
    }
}
