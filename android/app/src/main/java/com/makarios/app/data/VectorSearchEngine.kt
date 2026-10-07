package com.makarios.app.data

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.PriorityQueue
import kotlin.math.sqrt

data class VerseMatch(
    val reference: String,
    val text: String,
    val isDevotional: Boolean,
    val score: Float
)

data class BibleVerseEntry(
    val reference: String,
    val text: String,
    val isDevotional: Boolean
)

/**
 * On-device local vector search engine.
 *
 * Runs all-MiniLM-L12-v2 via ONNX Runtime to embed user declarations,
 * and performs cosine similarity search across all 31,000+ pre-computed
 * Bible verse vectors memory-mapped from assets/embeddings.bin.
 */
class VectorSearchEngine private constructor(private val context: Context) {

    private val tokenizer = BertTokenizer.getInstance(context)
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    private var verses: List<BibleVerseEntry> = emptyList()
    private var embeddingsBuffer: ByteBuffer? = null
    private var numVerses: Int = 0
    private var vectorDim: Int = 384

    private var isInitialized = false

    companion object {
        @Volatile
        private var instance: VectorSearchEngine? = null

        fun getInstance(context: Context): VectorSearchEngine {
            return instance ?: synchronized(this) {
                instance ?: VectorSearchEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Loads ONNX session, verses metadata, and memory-maps embeddings.bin.
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            // 1. Initialize ONNX runtime environment & session
            val env = OrtEnvironment.getEnvironment()
            ortEnv = env
            context.assets.open("minilm_l12_quantized.onnx").use { inputStream ->
                val modelBytes = inputStream.readBytes()
                ortSession = env.createSession(modelBytes)
            }

            // 2. Load verses.json
            context.assets.open("verses.json").use { inputStream ->
                val jsonString = InputStreamReader(inputStream, Charsets.UTF_8).readText()
                val jsonArray = JSONArray(jsonString)
                val parsed = ArrayList<BibleVerseEntry>(jsonArray.length())
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    parsed.add(
                        BibleVerseEntry(
                            reference = obj.getString("r"),
                            text = com.makarios.app.data.bible.ScriptureText.clean(obj.getString("t")),
                            isDevotional = obj.optInt("d", 0) == 1
                        )
                    )
                }
                verses = parsed
            }

            // 3. Memory-map embeddings.bin
            val assetFd = context.assets.openFd("embeddings.bin")
            assetFd.createInputStream().use { fis ->
                val channel = fis.channel
                val mapped = channel.map(FileChannel.MapMode.READ_ONLY, assetFd.startOffset, assetFd.length)
                mapped.order(ByteOrder.LITTLE_ENDIAN)

                numVerses = mapped.getInt(0)
                vectorDim = mapped.getInt(4)
                embeddingsBuffer = mapped
            }

            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Searches the entire 31,000+ Bible verse corpus for the closest semantic matches.
     *
     * Hybrid scoring:
     *   finalScore = (vectorCosineSimilarity × 0.7) + (normalisedKeywordScore × 0.3) × devotionalMultiplier
     *
     * Returns the top [topK] results sorted by hybrid score, descending.
     * Tone post-filtering is handled in the caller (CreateScreen) where ScriptureDatabase
     * tone metadata is available.
     *
     * @param query  The user's declaration text
     * @param topK   How many results to return (default 10)
     */
    suspend fun search(
        query: String,
        topK: Int = 10
    ): List<VerseMatch> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            initialize()
        }

        val queryVector = embedQuery(query) ?: return@withContext emptyList()
        val buffer = embeddingsBuffer ?: return@withContext emptyList()

        // Stemmed & synonym-expanded keywords for hybrid keyword component
        val queryKeywords = ScriptureMatcher.expandSynonyms(ScriptureMatcher.tokenize(query))

        // Duplicate buffer to be thread-safe; header is 8 bytes (2 ints)
        val dup = buffer.duplicate()
        dup.order(ByteOrder.LITTLE_ENDIAN)
        dup.position(8)
        val floatBuffer = dup.asFloatBuffer()
        val tempVerseVector = FloatArray(vectorDim)

        // Collect top results by hybrid score using a min-heap
        val minHeap = PriorityQueue<VerseMatch>(topK + 1, compareBy { it.score })

        for (i in 0 until numVerses) {
            floatBuffer.position(i * vectorDim)
            floatBuffer.get(tempVerseVector)

            // Both vectors are L2-normalized; cosine similarity = dot product
            var dot = 0f
            for (d in 0 until vectorDim) {
                dot += queryVector[d] * tempVerseVector[d]
            }

            val verse = verses.getOrNull(i) ?: continue

            // Keyword component — normalised to [0, 1] using stemmed tokens
            val keywordBonus = if (queryKeywords.isNotEmpty()) {
                val verseLower = verse.text.lowercase()
                val hits = queryKeywords.count { kw -> verseLower.contains(kw) }
                (hits.toFloat() / queryKeywords.size.toFloat()).coerceIn(0f, 1f)
            } else 0f

            // Hybrid: 70% semantic vector + 30% keyword overlap
            val hybridScore = (dot * 0.7f) + (keywordBonus * 0.3f)

            // Devotional weighting: 1.12x multiplier for curated promise verses
            val devotionalMultiplier = if (verse.isDevotional) 1.12f else 1.0f
            val finalScore = hybridScore * devotionalMultiplier

            val match = VerseMatch(
                reference = verse.reference,
                text = verse.text,
                isDevotional = verse.isDevotional,
                score = finalScore
            )

            minHeap.offer(match)
            if (minHeap.size > topK) {
                minHeap.poll()
            }
        }

        // Sort descending (best match first)
        val results = ArrayList<VerseMatch>(minHeap.size)
        while (minHeap.isNotEmpty()) results.add(minHeap.poll())
        results.reverse()
        results
    }

    private fun embedQuery(text: String): FloatArray? {
        val env = ortEnv ?: return null
        val session = ortSession ?: return null

        val tokenized = tokenizer.tokenize(text)
        val seqLen = tokenized.inputIds.size

        val inputIdsTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.inputIds))
        val attentionMaskTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.attentionMask))
        val tokenTypeIdsTensor = OnnxTensor.createTensor(env, arrayOf(tokenized.tokenTypeIds))

        val inputs = mapOf(
            "input_ids" to inputIdsTensor,
            "attention_mask" to attentionMaskTensor,
            "token_type_ids" to tokenTypeIdsTensor
        )

        session.run(inputs).use { result ->
            @Suppress("UNCHECKED_CAST")
            val output3D = result[0].value as Array<Array<FloatArray>>
            val lastHiddenState = output3D[0] // Shape: [seqLen, 384]

            // Mean pooling with attention mask
            val meanPooled = FloatArray(vectorDim)
            var tokenCount = 0f

            for (i in 0 until seqLen) {
                if (tokenized.attentionMask[i] == 1L) {
                    tokenCount += 1f
                    for (d in 0 until vectorDim) {
                        meanPooled[d] += lastHiddenState[i][d]
                    }
                }
            }

            if (tokenCount > 0f) {
                for (d in 0 until vectorDim) {
                    meanPooled[d] /= tokenCount
                }
            }

            // L2 normalize
            var sumSq = 0f
            for (v in meanPooled) {
                sumSq += v * v
            }
            val norm = sqrt(sumSq).coerceAtLeast(1e-12f)
            for (d in 0 until vectorDim) {
                meanPooled[d] /= norm
            }

            return meanPooled
        }
    }
}
