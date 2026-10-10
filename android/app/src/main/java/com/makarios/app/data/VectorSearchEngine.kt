package com.makarios.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VerseMatch(val reference: String, val text: String, val isDevotional: Boolean, val score: Float)

data class BibleVerseEntry(val reference: String, val text: String, val isDevotional: Boolean)

/** Compatibility facade. Matching no longer loads embeddings.bin or verses.json. */
class VectorSearchEngine private constructor(private val context: Context) {
    private val engine = MatchEngine.getInstance(context)
    val isReady: Boolean get() = engine.ready
    suspend fun initialize() = engine.initialize()
    suspend fun search(query: String, topK: Int = 10): List<VerseMatch> = withContext(Dispatchers.Default) { engine.match(query, topK).top }
    companion object {
        @Volatile private var instance: VectorSearchEngine? = null
        fun getInstance(context: Context): VectorSearchEngine = instance ?: synchronized(this) { instance ?: VectorSearchEngine(context.applicationContext).also { instance = it } }
    }
}