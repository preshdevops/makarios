package com.makarios.app.data.bible

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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

// The SQLite index stays on disk; only a requested chapter or limited search result enters heap.
enum class Testament { OT, NT }

data class BibleBook(val name: String, val testament: Testament, val chapterCount: Int)

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
    data object Loading : BibleState
    data object Ready : BibleState
    data class Failed(val error: Throwable) : BibleState
}

object BibleRepository {
    private const val TAG = "BibleRepository"
    private const val DB_NAME = "bible_index.db"
    private val initMutex = Mutex()
    private val _state = MutableStateFlow<BibleState>(BibleState.Loading)
    val state: StateFlow<BibleState> = _state.asStateFlow()
    @Volatile private var booksSnapshot: List<BibleBook> = emptyList()
    @Volatile private var database: SQLiteDatabase? = null

    val isReady: Boolean get() = _state.value is BibleState.Ready
    val books: List<BibleBook> get() = booksSnapshot

    /** Builds a disk-backed index from the Bible asset only when a Bible view or search is opened. */
    suspend fun initialize(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        if (isReady) return@withContext Result.success(Unit)
        initMutex.withLock {
            if (isReady) return@withLock Result.success(Unit)
            _state.value = BibleState.Loading
            val startedAt = System.currentTimeMillis()
            try {
                val db = BibleIndexHelper(context.applicationContext).writableDatabase
                val count = db.rawQuery("SELECT COUNT(*) FROM verses", null).use { cursor ->
                    if (cursor.moveToFirst()) cursor.getInt(0) else 0
                }
                if (count == 0) importVerses(context, db)
                loadBooks(db)
                database = db
                _state.value = BibleState.Ready
                Log.d(TAG, "SQLite index ready in ${System.currentTimeMillis() - startedAt}ms")
                Result.success(Unit)
            } catch (failure: Throwable) {
                _state.value = BibleState.Failed(failure)
                Log.e(TAG, "Failed to build Bible SQLite index", failure)
                Result.failure(failure)
            }
        }
    }

    private fun importVerses(context: Context, db: SQLiteDatabase) {
        val insert = db.compileStatement(
            "INSERT OR REPLACE INTO verses(reference, reference_key, book, chapter, verse, text) VALUES(?, ?, ?, ?, ?, ?)"
        )
        db.beginTransaction()
        try {
            context.assets.open("verses.json").use { input ->
                JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    reader.beginArray()
                    while (reader.hasNext()) {
                        var rawReference = ""
                        var rawText = ""
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "r" -> rawReference = reader.nextString()
                                "t" -> rawText = reader.nextString()
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()
                        val reference = ScriptureText.normalizeReference(rawReference)
                        val parts = splitReference(reference) ?: continue
                        insert.clearBindings()
                        insert.bindString(1, reference)
                        insert.bindString(2, reference.lowercase())
                        insert.bindString(3, parts.first)
                        insert.bindLong(4, parts.second.toLong())
                        insert.bindLong(5, parts.third.toLong())
                        insert.bindString(6, ScriptureText.clean(rawText))
                        insert.executeInsert()
                    }
                    reader.endArray()
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            insert.close()
        }
    }

    private fun loadBooks(db: SQLiteDatabase) {
        val chapterCounts = mutableMapOf<String, Int>()
        db.rawQuery("SELECT book, MAX(chapter) FROM verses GROUP BY book", null).use { cursor ->
            while (cursor.moveToNext()) chapterCounts[cursor.getString(0)] = cursor.getInt(1)
        }
        val books = ArrayList<BibleBook>(BibleReferenceParser.CANONICAL_BOOKS.size)
        BibleReferenceParser.CANONICAL_BOOKS.forEachIndexed { index, book ->
            val testament = if (index < 39) Testament.OT else Testament.NT
            books.add(BibleBook(book, testament, chapterCounts[book] ?: 1))
        }
        // Publish atomically: Compose can read this on the main thread while initialization
        // runs on Dispatchers.IO.
        booksSnapshot = books.toList()
    }

    private fun splitReference(reference: String): Triple<String, Int, Int>? {
        val lastSpace = reference.lastIndexOf(' ')
        if (lastSpace <= 0) return null
        val book = reference.substring(0, lastSpace)
        val chapterVerse = reference.substring(lastSpace + 1)
        val colon = chapterVerse.indexOf(':')
        if (colon <= 0) return null
        val chapter = chapterVerse.substring(0, colon).toIntOrNull() ?: return null
        val verse = chapterVerse.substring(colon + 1).toIntOrNull() ?: return null
        return Triple(book, chapter, verse)
    }

    fun getVerse(reference: String): Result<BibleVerse?> = runCatching {
        val db = database ?: return@runCatching null
        val normalized = ScriptureText.normalizeReference(reference)
        db.rawQuery(
            "SELECT book, chapter, verse, text, reference FROM verses WHERE reference_key = ? LIMIT 1",
            arrayOf(normalized.lowercase())
        ).use { cursor -> if (cursor.moveToFirst()) cursor.toBibleVerse() else null }
    }

    fun getVerse(context: Context, reference: String): BibleVerse? = getVerse(reference).getOrNull()

    fun getChapter(book: String, chapter: Int): Result<List<BibleVerse>> = runCatching {
        val db = database ?: throw IllegalStateException("Bible index has not been initialized")
        val canonical = BibleReferenceParser.resolveBookName(book) ?: book
        db.rawQuery(
            "SELECT book, chapter, verse, text, reference FROM verses WHERE book = ? AND chapter = ? ORDER BY verse",
            arrayOf(canonical, chapter.toString())
        ).use { cursor ->
            val verses = ArrayList<BibleVerse>()
            while (cursor.moveToNext()) verses.add(cursor.toBibleVerse())
            if (verses.isEmpty()) throw NoSuchElementException("Chapter $chapter of $book is not found.")
            verses
        }
    }

    fun getChapter(context: Context, book: String, chapter: Int): List<BibleVerse> =
        getChapter(book, chapter).getOrElse { emptyList() }

    fun getRange(book: String, chapter: Int, fromVerse: Int, toVerse: Int): Result<List<BibleVerse>> = runCatching {
        val db = database ?: throw IllegalStateException("Bible index has not been initialized")
        val canonical = BibleReferenceParser.resolveBookName(book) ?: book
        val first = minOf(fromVerse, toVerse)
        val last = minOf(maxOf(fromVerse, toVerse), first + 7)
        db.rawQuery(
            "SELECT book, chapter, verse, text, reference FROM verses WHERE book = ? AND chapter = ? AND verse BETWEEN ? AND ? ORDER BY verse",
            arrayOf(canonical, chapter.toString(), first.toString(), last.toString())
        ).use { cursor ->
            val verses = ArrayList<BibleVerse>()
            while (cursor.moveToNext()) verses.add(cursor.toBibleVerse())
            verses
        }
    }

    fun getRange(context: Context, book: String, chapter: Int, fromVerse: Int, toVerse: Int): List<BibleVerse> =
        getRange(book, chapter, fromVerse, toVerse).getOrElse { emptyList() }

    fun searchVerses(query: String, limit: Int = 20): Result<List<BibleVerse>> = runCatching {
        val db = database ?: return@runCatching emptyList()
        val term = query.trim()
        if (term.length < 2) return@runCatching emptyList()
        val wordRegex = Regex("\\b${Regex.escape(term)}\\b", RegexOption.IGNORE_CASE)
        val exact = ArrayList<BibleVerse>(limit)
        val partial = ArrayList<BibleVerse>(limit)
        db.rawQuery(
            "SELECT book, chapter, verse, text, reference FROM verses WHERE text LIKE ? COLLATE NOCASE ORDER BY book, chapter, verse",
            arrayOf("%$term%")
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val verse = cursor.toBibleVerse()
                if (wordRegex.containsMatchIn(verse.text)) {
                    exact.add(verse)
                    if (exact.size >= limit) break
                } else if (partial.size < limit) partial.add(verse)
            }
        }
        if (exact.size >= limit) exact else (exact + partial).distinctBy { it.reference }.take(limit)
    }

    fun searchVerses(context: Context, query: String, limit: Int = 20): List<BibleVerse> =
        searchVerses(query, limit).getOrElse { emptyList() }

    private fun Cursor.toBibleVerse() = BibleVerse(
        book = getString(0), chapter = getInt(1), verse = getInt(2), text = getString(3), reference = getString(4)
    )

    private class BibleIndexHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE verses (reference TEXT NOT NULL, reference_key TEXT PRIMARY KEY NOT NULL, book TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, text TEXT NOT NULL)")
            db.execSQL("CREATE INDEX idx_verses_book_chapter_verse ON verses(book, chapter, verse)")
            db.execSQL("CREATE INDEX idx_verses_book ON verses(book)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS verses")
            onCreate(db)
        }
    }
}