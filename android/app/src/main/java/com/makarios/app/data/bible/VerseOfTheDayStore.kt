package com.makarios.app.data.bible

import android.content.Context
import android.util.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.Calendar

 data class VerseOfTheDay(
    val reference: String,
    val text: String,
    val book: String,
    val chapter: Int,
    val verse: Int
)

object VerseOfTheDayStore {
    @Volatile private var cachedList: List<String>? = null

    fun getTodayReference(context: Context): String {
        val list = loadReferences(context)
        if (list.isEmpty()) return "Philippians 4:13"
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return list[(dayOfYear - 1).coerceAtLeast(0) % list.size]
    }

    fun fallbackVerse() = VerseOfTheDay(
        reference = "Philippians 4:13",
        text = "I can do all things through Christ, who strengthens me.",
        book = "Philippians",
        chapter = 4,
        verse = 13
    )

    /** Reads just the selected VOTD verse from the streamed asset; it does not initialize the Bible index. */
    suspend fun loadTodayVerse(context: Context): VerseOfTheDay = withContext(Dispatchers.IO) {
        val reference = getTodayReference(context)
        val parsed = BibleReferenceParser.parse(reference)
        val book = parsed?.book ?: "Philippians"
        val chapter = parsed?.chapter ?: 4
        val verseNumber = parsed?.fromVerse ?: 13
        val verse = BibleRepository.getVerse(reference).getOrNull()
            ?: findInVerseAsset(context, reference)
        VerseOfTheDay(
            reference = ScriptureText.formatDisplayReference(verse?.reference ?: reference),
            text = verse?.text ?: fallbackVerse().text,
            book = verse?.book ?: book,
            chapter = verse?.chapter ?: chapter,
            verse = verse?.verse ?: verseNumber
        )
    }

    fun getTodayVerse(context: Context): VerseOfTheDay {
        val ref = getTodayReference(context)
        val parsed = BibleReferenceParser.parse(ref)
        val verse = BibleRepository.getVerse(ref).getOrNull()
        val fallback = fallbackVerse()
        return VerseOfTheDay(
            reference = ScriptureText.formatDisplayReference(verse?.reference ?: ref),
            text = verse?.text ?: fallback.text,
            book = verse?.book ?: parsed?.book ?: fallback.book,
            chapter = verse?.chapter ?: parsed?.chapter ?: fallback.chapter,
            verse = verse?.verse ?: parsed?.fromVerse ?: fallback.verse
        )
    }

    private fun findInVerseAsset(context: Context, reference: String): BibleVerse? {
        val wanted = ScriptureText.normalizeReference(reference).lowercase()
        val parsed = BibleReferenceParser.parse(reference) ?: return null
        val verseNumber = parsed.fromVerse ?: return null
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
                    val normalized = ScriptureText.normalizeReference(rawReference)
                    if (normalized.equals(wanted, ignoreCase = true)) {
                        return BibleVerse(
                            book = parsed.book,
                            chapter = parsed.chapter,
                            verse = verseNumber,
                            text = ScriptureText.clean(rawText),
                            reference = normalized
                        )
                    }
                }
                reader.endArray()
            }
        }
        return null
    }

    private fun loadReferences(context: Context): List<String> {
        cachedList?.let { return it }
        return synchronized(this) {
            cachedList ?: runCatching {
                val list = mutableListOf<String>()
                context.assets.open("verse_of_the_day.json").use { input ->
                    JsonReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                        reader.beginArray()
                        while (reader.hasNext()) list.add(reader.nextString())
                        reader.endArray()
                    }
                }
                list
            }.getOrElse {
                listOf("Philippians 4:13", "Isaiah 40:31", "Romans 8:28", "Psalms 23:1")
            }.also { cachedList = it }
        }
    }
}