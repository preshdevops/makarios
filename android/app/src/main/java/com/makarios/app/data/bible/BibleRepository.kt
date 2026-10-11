package com.makarios.app.data.bible

import android.content.Context
import android.util.JsonReader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

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

sealed interface BibleState {
    object Loading : BibleState
    object Ready : BibleState
    data class Failed(val error: Throwable) : BibleState
}

object BibleRepository {

    private const val TAG = "BibleRepository"
    private val initMutex = Mutex()

    private val _state = MutableStateFlow<BibleState>(BibleState.Loading)
    val state: StateFlow<BibleState> = _state.asStateFlow()

    private val booksList = mutableListOf<BibleBook>()
    private val chapterMap = HashMap<String, MutableMap<Int, MutableList<BibleVerse>>>()
    private val verseMap = HashMap<String, BibleVerse>()
    private val allVerses = mutableListOf<BibleVerse>()

    val isReady: Boolean get() = _state.value is BibleState.Ready

    val books: List<BibleBook>
        get() = booksList

    /**
     * Initializes the in-memory Bible index using a streaming JsonReader from assets/verses.json.
     * Loads once on Dispatchers.IO and stays in memory for instant (<50ms) access.
     */
    suspend fun initialize(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (_state.value is BibleState.Ready) return@withContext Result.success(Unit)
        initMutex.withLock {
            if (_state.value is BibleState.Ready) return@withLock Result.success(Unit)
            _state.value = BibleState.Loading
            val start = System.currentTimeMillis()
            try {
                booksList.clear()
                chapterMap.clear()
                verseMap.clear()
                allVerses.clear()

                val bookMaxChapters = HashMap<String, Int>()

                context.assets.open("verses.json").use { inputStream ->
                    JsonReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            var rawRef = ""
                            var rawText = ""
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "r" -> rawRef = reader.nextString()
                                    "t" -> rawText = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()

                            val normRef = ScriptureText.normalizeReference(rawRef)
                            val cleanText = ScriptureText.clean(rawText)

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
                        reader.endArray()
                    }
                }

                // Populate canonical book list
                val canonicalOrder = BibleReferenceParser.CANONICAL_BOOKS
                canonicalOrder.forEachIndexed { index, name ->
                    val chCount = bookMaxChapters[name] ?: 1
                    val testament = if (index < 39) Testament.OT else Testament.NT
                    booksList.add(BibleBook(name, testament, chCount))
                }

                _state.value = BibleState.Ready
                Log.d(TAG, "Stream loaded ${allVerses.size} verses across ${booksList.size} books in ${System.currentTimeMillis() - start}ms")
                Result.success(Unit)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to stream load verses.json", t)
                _state.value = BibleState.Failed(t)
                Result.failure(t)
            }
        }
    }

    fun getVerse(reference: String): Result<BibleVerse?> {
        return runCatching {
            val parsed = BibleReferenceParser.parse(reference)
            if (parsed != null && parsed.fromVerse != null) {
                val key = "${parsed.book} ${parsed.chapter}:${parsed.fromVerse}".lowercase()
                verseMap[key]
            } else {
                val cleanKey = ScriptureText.normalizeReference(reference).lowercase()
                verseMap[cleanKey]
            }
        }
    }

    // Overload for backward compatibility
    fun getVerse(context: Context, reference: String): BibleVerse? = getVerse(reference).getOrNull()

    fun getChapter(book: String, chapter: Int): Result<List<BibleVerse>> {
        return runCatching {
            val canonical = BibleReferenceParser.resolveBookName(book) ?: book
            val chList = chapterMap[canonical]?.get(chapter)
            if (chList.isNullOrEmpty()) {
                throw NoSuchElementException("Chapter $chapter of $book is not found.")
            }
            chList.toList()
        }
    }

    // Overload for backward compatibility
    fun getChapter(context: Context, book: String, chapter: Int): List<BibleVerse> =
        getChapter(book, chapter).getOrElse { emptyList() }

    fun getRange(
        book: String,
        chapter: Int,
        fromVerse: Int,
        toVerse: Int
    ): Result<List<BibleVerse>> {
        return runCatching {
            val chResult = getChapter(book, chapter).getOrThrow()
            val minV = minOf(fromVerse, toVerse)
            val maxV = maxOf(fromVerse, toVerse)
            val cappedMax = minOf(maxV, minV + 7)
            chResult.filter { it.verse in minV..cappedMax }
        }
    }

    // Overload for backward compatibility
    fun getRange(context: Context, book: String, chapter: Int, fromVerse: Int, toVerse: Int): List<BibleVerse> =
        getRange(book, chapter, fromVerse, toVerse).getOrElse { emptyList() }

    fun searchVerses(query: String, limit: Int = 20): Result<List<BibleVerse>> {
        return runCatching {
            val q = query.trim().lowercase()
            if (q.length < 2) return@runCatching emptyList()

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

    // Overload for backward compatibility
    fun searchVerses(context: Context, query: String, limit: Int = 20): List<BibleVerse> =
        searchVerses(query, limit).getOrElse { emptyList() }
}
