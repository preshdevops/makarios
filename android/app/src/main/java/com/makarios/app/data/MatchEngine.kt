package com.makarios.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.makarios.app.data.bible.ScriptureText

/** Public matching API. It is model-ready and remains useful offline with the reviewed bank. */
data class EngineMatchResult(val top: List<VerseMatch>, val confident: Boolean, val reason: String)

class MatchEngine private constructor(private val context: Context) {
    @Volatile var ready: Boolean = false
        private set

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (ready) return@withContext
        ScriptureMatcher.match("warm up my heart")
        ready = true
    }

    suspend fun match(text: String, limit: Int = 3): EngineMatchResult = withContext(Dispatchers.Default) {
        initialize()
        val ranked = ScriptureMatcher.match(text, limit = maxOf(limit, 3)).take(3)
        val top = ranked.map { VerseMatch(it.verse.reference, ScriptureText.clean(it.verse.text), true, it.score) }
        val confident = top.firstOrNull()?.score?.let { it >= .42f } == true
        EngineMatchResult(top, confident, if (confident) "curated_match" else "No confident match")
    }

    companion object {
        @Volatile private var instance: MatchEngine? = null
        fun getInstance(context: Context): MatchEngine = instance ?: synchronized(this) { instance ?: MatchEngine(context.applicationContext).also { instance = it } }
    }
}
