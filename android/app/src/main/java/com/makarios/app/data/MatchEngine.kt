package com.makarios.app.data

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.makarios.app.data.bible.ScriptureText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.sqrt

/** Public matching API. Returns top matches, confidence flag, and engine reason. */
data class EngineMatchResult(val top: List<VerseMatch>, val confident: Boolean, val reason: String)

class MatchEngine private constructor(private val context: Context?) {

    data class IndexedVerse(
        val id: Int,
        val ref: String,
        val text: String,
        val book: String,
        val reason: String,
        val rowIndices: MutableList<Int> = mutableListOf()
    )

    data class ReviewedBankEntry(
        val phrase: String,
        val refs: List<String>,
        val reason: String,
        var embedding: FloatArray? = null
    )

    @Volatile var ready: Boolean = false
        private set

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var tokenizer: BertTokenizer? = null

    private var dim: Int = 384
    private var totalRows: Int = 0
    private var embMatrix: ByteArray? = null
    private var rowWeights: FloatArray = FloatArray(0)
    private var verses: List<IndexedVerse> = emptyList()
    private var verseByRef: Map<String, IndexedVerse> = emptyMap()
    private var reviewedBank: List<ReviewedBankEntry> = emptyList()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (ready) return@withContext
        try {
            if (context != null) {
                initializeFromAssets(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ready = true
    }

    fun initializeFromStreams(
        modelStream: InputStream,
        vocabStream: InputStream,
        indexJsonStream: InputStream,
        embeddingsBinStream: InputStream,
        seedJsonStream: InputStream,
        bankJsonStream: InputStream
    ) {
        val env = OrtEnvironment.getEnvironment()
        ortEnv = env
        val modelBytes = modelStream.readBytes()
        ortSession = env.createSession(modelBytes, OrtSession.SessionOptions())
        tokenizer = BertTokenizer.loadFromStream(vocabStream)

        // Load index.json
        val indexJsonStr = indexJsonStream.bufferedReader().use { it.readText() }
        val root = JSONObject(indexJsonStr)
        dim = root.optInt("dim", 384)
        totalRows = root.optInt("total_rows", 0)

        val versesArray = root.getJSONArray("verses")
        val vList = ArrayList<IndexedVerse>(versesArray.length())
        val vMap = mutableMapOf<String, IndexedVerse>()
        for (i in 0 until versesArray.length()) {
            val obj = versesArray.getJSONObject(i)
            val v = IndexedVerse(
                id = obj.getInt("id"),
                ref = obj.getString("ref"),
                text = obj.getString("text"),
                book = obj.getString("book"),
                reason = obj.getString("reason")
            )
            vList.add(v)
            vMap[v.ref] = v
            if (v.ref.startsWith("Psalms ")) {
                vMap["Psalm " + v.ref.removePrefix("Psalms ")] = v
            } else if (v.ref.startsWith("Psalm ")) {
                vMap["Psalms " + v.ref.removePrefix("Psalm ")] = v
            }
        }
        verses = vList
        verseByRef = vMap

        val rowsArray = root.getJSONArray("rows")
        val weights = FloatArray(rowsArray.length())
        for (r in 0 until rowsArray.length()) {
            val rObj = rowsArray.getJSONObject(r)
            val vId = rObj.getInt("v")
            val w = rObj.getDouble("w").toFloat()
            weights[r] = w
            if (vId in 0 until vList.size) {
                vList[vId].rowIndices.add(r)
            }
        }
        rowWeights = weights

        // Load embeddings_i8.bin
        embMatrix = embeddingsBinStream.readBytes()

        // Load reviewed bank entries (seeds + bank)
        val bankList = mutableListOf<ReviewedBankEntry>()

        val seedStr = seedJsonStream.bufferedReader().use { it.readText() }
        val seedArr = org.json.JSONArray(seedStr)
        for (i in 0 until seedArr.length()) {
            val sObj = seedArr.getJSONObject(i)
            val decl = sObj.getString("declaration")
            val vArr = sObj.getJSONArray("verses")
            val refs = mutableListOf<String>()
            for (j in 0 until vArr.length()) {
                refs.add(vArr.getJSONObject(j).getString("ref"))
            }
            bankList.add(ReviewedBankEntry(decl, refs, "Matches: $decl"))
        }

        val bankStr = bankJsonStream.bufferedReader().use { it.readText() }
        val bankArr = org.json.JSONArray(bankStr)
        for (i in 0 until bankArr.length()) {
            val bObj = bankArr.getJSONObject(i)
            val decl = bObj.getString("declaration")
            val rArr = bObj.optJSONArray("references") ?: org.json.JSONArray()
            val refs = mutableListOf<String>()
            for (j in 0 until rArr.length()) {
                refs.add(rArr.getString(j))
            }
            if (refs.isNotEmpty()) {
                bankList.add(ReviewedBankEntry(decl, refs, "Matches: $decl"))
                val aliasArr = bObj.optJSONArray("aliases")
                if (aliasArr != null) {
                    val maxAliases = minOf(2, aliasArr.length())
                    for (k in 0 until maxAliases) {
                        bankList.add(ReviewedBankEntry(aliasArr.getString(k), refs, "Matches: $decl"))
                    }
                }
            }
        }

        // Precompute reviewed bank embeddings
        for (entry in bankList) {
            entry.embedding = computeEmbedding(entry.phrase)
        }
        reviewedBank = bankList
        ready = true
    }

    private fun initializeFromAssets(ctx: Context) {
        val am = ctx.assets
        am.open("minilm_l12_quantized.onnx").use { mStream ->
            am.open("vocab.txt").use { vocStream ->
                am.open("index.json").use { idxStream ->
                    am.open("embeddings_i8.bin").use { embStream ->
                        am.open("declarations_seed.json").use { seedStream ->
                            am.open("declaration_bank.json").use { bankStream ->
                                initializeFromStreams(
                                    mStream,
                                    vocStream,
                                    idxStream,
                                    embStream,
                                    seedStream,
                                    bankStream
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun computeEmbedding(text: String): FloatArray? {
        val tok = tokenizer ?: return null
        val session = ortSession ?: return null
        val env = ortEnv ?: return null

        val tokenized = tok.tokenize(text, maxSeqLength = 64)
        val shape = longArrayOf(1L, tokenized.inputIds.size.toLong())
        val inputIdsTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.inputIds))
        val maskTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.attentionMask))
        val typeIdsTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.tokenTypeIds))

        val inputs = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to maskTensor,
            "token_type_ids" to typeIdsTensor
        )

        return try {
            val outputs = session.run(inputs)
            @Suppress("UNCHECKED_CAST")
            val lastHidden = outputs.get(0).value as Array<Array<FloatArray>>
            val seqLen = tokenized.inputIds.size
            val pooled = FloatArray(dim)
            var count = 0f
            for (i in 0 until seqLen) {
                if (tokenized.attentionMask[i] == 1L) {
                    val h = lastHidden[0][i]
                    for (d in 0 until dim) {
                        pooled[d] += h[d]
                    }
                    count += 1f
                }
            }
            if (count > 0f) {
                for (d in 0 until dim) pooled[d] /= count
            }
            var sumSq = 0f
            for (d in 0 until dim) sumSq += pooled[d] * pooled[d]
            val norm = sqrt(sumSq).coerceAtLeast(1e-9f)
            for (d in 0 until dim) pooled[d] /= norm
            outputs.close()
            pooled
        } finally {
            inputIdsTensor.close()
            maskTensor.close()
            typeIdsTensor.close()
        }
    }

    private fun expandQuery(text: String): String {
        val lower = text.lowercase().trim()
        var expanded = text
        if ("inside" in lower || "within me" in lower || "in me" in lower) {
            expanded += " in you, indwelling, Christ in you, Spirit within"
        }
        if ("can do anything" in lower || "literally do anything" in lower || "can do all things" in lower) {
            expanded += " all things through Christ"
        }
        if ("never stranded" in lower || "not alone" in lower || "never alone" in lower) {
            expanded += " never leave nor forsake"
        }
        if ("safe" in lower || "hiding" in lower || "refuge" in lower) {
            expanded += " Psalm 91, Colossians 3:3 refuge hiding place"
        }
        return expanded
    }

    suspend fun match(text: String, limit: Int = 3): EngineMatchResult = withContext(Dispatchers.Default) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext EngineMatchResult(emptyList(), false, "No confident match")
        }

        if (!ready) {
            initialize()
        }

        val rawEmb = computeEmbedding(trimmed)
        if (rawEmb == null || embMatrix == null) {
            // Fallback to token matcher if ONNX is unavailable
            val ranked = ScriptureMatcher.match(trimmed, limit = maxOf(limit, 3)).take(limit)
            val top = ranked.map { VerseMatch(it.verse.reference, ScriptureText.clean(it.verse.text), true, it.score) }
            val confident = top.firstOrNull()?.score?.let { it >= 0.35f } == true
            return@withContext EngineMatchResult(top, confident, if (confident) "curated_match" else "No confident match")
        }

        // Rule 1: Reviewed bank exact or near match (cosine >= 0.80)
        var bestBankScore = -1f
        var bestBankEntry: ReviewedBankEntry? = null
        for (entry in reviewedBank) {
            val bEmb = entry.embedding ?: continue
            var dot = 0f
            for (d in 0 until dim) dot += rawEmb[d] * bEmb[d]
            if (dot > bestBankScore) {
                bestBankScore = dot
                bestBankEntry = entry
            }
        }

        if (bestBankScore >= 0.80f && bestBankEntry != null) {
            val curatedMatches = mutableListOf<VerseMatch>()
            val seenBooks = mutableSetOf<String>()
            for (ref in bestBankEntry.refs) {
                val v = verseByRef[ref]
                if (v != null && v.book !in seenBooks) {
                    curatedMatches.add(VerseMatch(v.ref, ScriptureText.clean(v.text), true, bestBankScore))
                    seenBooks.add(v.book)
                    if (curatedMatches.size >= limit) break
                }
            }
            if (curatedMatches.isNotEmpty()) {
                return@withContext EngineMatchResult(curatedMatches, true, bestBankEntry.reason)
            }
        }

        // Rule 2: Vector embedding search on int8 matrix
        val expandedText = expandQuery(trimmed)
        val expEmb = if (expandedText == trimmed) rawEmb else (computeEmbedding(expandedText) ?: rawEmb)
        val matrix = embMatrix ?: return@withContext EngineMatchResult(emptyList(), false, "No confident match")

        val rowScores = FloatArray(totalRows)
        for (r in 0 until totalRows) {
            var dot = 0f
            val offset = r * dim
            for (d in 0 until dim) {
                dot += expEmb[d] * (matrix[offset + d] / 127f)
            }
            rowScores[r] = dot
        }

        val verseScores = ArrayList<Pair<Float, IndexedVerse>>(verses.size)
        for (v in verses) {
            var best = -1f
            for (rIdx in v.rowIndices) {
                val s = rowScores[rIdx] * rowWeights[rIdx]
                if (s > best) best = s
            }
            verseScores.add(Pair(best, v))
        }
        verseScores.sortByDescending { it.first }

        val topScore = verseScores.firstOrNull()?.first ?: 0f
        val confident = topScore >= 0.42f
        if (!confident || verseScores.isEmpty()) {
            return@withContext EngineMatchResult(emptyList(), false, "No confident match")
        }

        // Rule 3: Book diversity - at most 1 verse per book in top 3 unless within 0.02
        val results = mutableListOf<VerseMatch>()
        val seenBooks = mutableSetOf<String>()
        val primaryReason = verseScores[0].second.reason

        for ((idx, pair) in verseScores.withIndex()) {
            val score = pair.first
            val v = pair.second
            if (idx == 0) {
                results.add(VerseMatch(v.ref, ScriptureText.clean(v.text), true, score))
                seenBooks.add(v.book)
            } else if (results.size < limit) {
                if (v.book !in seenBooks) {
                    results.add(VerseMatch(v.ref, ScriptureText.clean(v.text), true, score))
                    seenBooks.add(v.book)
                } else if (results.size == 1 && abs(score - verseScores[0].first) <= 0.02f) {
                    results.add(VerseMatch(v.ref, ScriptureText.clean(v.text), true, score))
                }
            }
        }

        EngineMatchResult(results, true, primaryReason)
    }

    companion object {
        @Volatile private var instance: MatchEngine? = null

        fun getInstance(context: Context): MatchEngine =
            instance ?: synchronized(this) {
                instance ?: MatchEngine(context.applicationContext).also { instance = it }
            }

        fun createStandalone(): MatchEngine = MatchEngine(null)
    }
}
