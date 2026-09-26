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
                            text = obj.getString("t"),
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

                numVerses = mapped.int
                vectorDim = mapped.int
                embeddingsBuffer = mapped
            }

            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Searches the entire 31,000+ Bible verse corpus for the closest semantic matches.
     * Returns topK ranked verses with devotional weighting.
     */
    suspend fun search(query: String, topK: Int = 8): List<VerseMatch> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            initialize()
        }

        val queryVector = embedQuery(query) ?: return@withContext emptyList()
        val buffer = embeddingsBuffer ?: return@withContext emptyList()

        val minHeap = PriorityQueue<VerseMatch>(topK + 1, compareBy { it.score })

        val floatBuffer = buffer.asFloatBuffer()
        val floatsHeaderOffset = 2 // numVerses (int) + vectorDim (int) = 2 floats
        val tempVerseVector = FloatArray(vectorDim)

        for (i in 0 until numVerses) {
            floatBuffer.position(floatsHeaderOffset + (i * vectorDim))
            floatBuffer.get(tempVerseVector)

            // Both vectors are L2-normalized, so cosine similarity is the dot product
            var dot = 0f
            for (d in 0 until vectorDim) {
                dot += queryVector[d] * tempVerseVector[d]
            }

            val verse = verses.getOrNull(i) ?: continue

            // Devotional Weighting: 1.15x multiplier if verse is in the devotional canon
            val finalScore = if (verse.isDevotional) dot * 1.15f else dot

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

        val results = ArrayList<VerseMatch>(minHeap.size)
        while (minHeap.isNotEmpty()) {
            results.add(minHeap.poll())
        }
        results.reverse() // Sort descending (best match first)
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
