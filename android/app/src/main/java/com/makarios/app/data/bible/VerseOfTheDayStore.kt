package com.makarios.app.data.bible

import android.content.Context
import org.json.JSONArray
import java.util.Calendar

data class VerseOfTheDay(
    val reference: String,
    val text: String,
    val book: String,
    val chapter: Int,
    val verse: Int
)

object VerseOfTheDayStore {
    @Volatile
    private var cachedList: List<String>? = null

    fun getTodayReference(context: Context): String {
        val list = loadReferences(context)
        if (list.isEmpty()) return "Philippians 4:13"
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1).coerceAtLeast(0) % list.size
        return list[index]
    }

    fun getTodayVerse(context: Context): VerseOfTheDay {
        val ref = getTodayReference(context)
        val parsed = BibleReferenceParser.parse(ref)
        val book = parsed?.book ?: "Philippians"
        val chapter = parsed?.chapter ?: 4
        val verseNum = parsed?.fromVerse ?: 13

        val verseObj = BibleRepository.getVerse(ref).getOrNull()
        val text = verseObj?.text ?: "I can do all things through Christ, who strengthens me."
        val displayRef = ScriptureText.formatDisplayReference(verseObj?.reference ?: ref)

        return VerseOfTheDay(
            reference = displayRef,
            text = text,
            book = book,
            chapter = chapter,
            verse = verseNum
        )
    }

    private fun loadReferences(context: Context): List<String> {
        cachedList?.let { return it }
        return synchronized(this) {
            cachedList ?: runCatching {
                val json = context.assets.open("verse_of_the_day.json").bufferedReader().use { it.readText() }
                val array = JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                cachedList = list
                list
            }.getOrElse {
                listOf("Philippians 4:13", "Isaiah 40:31", "Romans 8:28", "Psalms 23:1")
            }.also { cachedList = it }
        }
    }
}
