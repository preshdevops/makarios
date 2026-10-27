package com.makarios.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.makarios.app.data.bible.BibleReferenceParser
import com.makarios.app.data.bible.BibleVerse
import com.makarios.app.data.bible.ScriptureText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

data class BibleVersion(
    val code: String,
    val displayName: String,
    val fullTitle: String,
    val isOffline: Boolean = true,
    val note: String
)

object BibleVersionRepository {
    val availableVersions: List<BibleVersion> = listOf(
        BibleVersion("web", "WEB", "World English Bible", true, "Modern and clear"),
        BibleVersion("kjv", "KJV", "King James Version", true, "Classic and poetic"),
        BibleVersion("asv", "ASV", "American Standard Version", true, "Literal and faithful")
    )

    @Volatile private var database: SQLiteDatabase? = null
    private val mutex = Mutex()

    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (database != null) return@withContext
        mutex.withLock {
            if (database != null) return@withLock
            val file = File(context.applicationContext.filesDir, "bible_packs.db")
            if (!file.exists() || file.length() == 0L) {
                context.applicationContext.assets.open("bible_packs.db").use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
            database = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }
    }

    suspend fun getChapter(
        context: Context,
        book: String,
        chapter: Int,
        version: BibleVersion,
        fallback: List<BibleVerse> = emptyList()
    ): List<BibleVerse> = withContext(Dispatchers.IO) {
        if (version.code.equals("web", true)) return@withContext fallback
        initialize(context)
        val canonical = BibleReferenceParser.resolveBookName(book) ?: book
        val rows = database?.rawQuery(
            "SELECT verse, text FROM verses WHERE version = ? AND book = ? AND chapter = ? ORDER BY verse",
            arrayOf(version.displayName.uppercase(), canonical, chapter.toString())
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val verse = cursor.getInt(0)
                    add(BibleVerse(canonical, chapter, verse, cursor.getString(1), "$canonical $chapter:$verse"))
                }
            }
        }.orEmpty()
        if (rows.isEmpty()) fallback else rows
    }

    suspend fun getVerseText(
        context: Context,
        reference: String,
        fallbackText: String,
        version: BibleVersion
    ): String = withContext(Dispatchers.IO) {
        if (version.code.equals("web", true)) return@withContext ScriptureText.clean(fallbackText)
        val parsed = BibleReferenceParser.parse(reference) ?: return@withContext ScriptureText.clean(fallbackText)
        val chapter = getChapter(context, parsed.book, parsed.chapter, version)
        chapter.firstOrNull { it.verse == parsed.fromVerse }?.text ?: ScriptureText.clean(fallbackText)
    }
}
