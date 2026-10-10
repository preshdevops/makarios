package com.makarios.app.data.bible

import android.content.Context
import org.json.JSONArray

object RecentVersesStore {
    private const val PREFS = "makarios_recent_verses"
    private const val KEY = "recents"

    fun getRecents(context: Context): List<String> {
        return runCatching {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY, null) ?: return listOf(
                "John 3:16", "Romans 8:28", "Psalm 23:1", "Philippians 4:13"
            )
            val array = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        }.getOrDefault(listOf("John 3:16", "Romans 8:28", "Psalm 23:1", "Philippians 4:13"))
    }

    fun addRecent(context: Context, reference: String) {
        runCatching {
            val current = getRecents(context).filter { !it.equals(reference, ignoreCase = true) }
            val updated = (listOf(reference) + current).take(8)
            val array = JSONArray()
            updated.forEach { array.put(it) }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY, array.toString())
                .apply()
        }
    }
}
