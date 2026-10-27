package com.makarios.app.data

import android.content.Context
import android.os.SystemClock
import android.util.JsonReader
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.makarios.app.data.bible.ScriptureText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.makarios.app.util.PerformanceMetrics
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
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

    @Volatile var isInitializing: Boolean = false
        private set

    private val initializeMutex = Mutex()
    @Volatile private var initializationAttempted = false
    @Volatile private var retryPendingForeground = false
    @Volatile private var foregroundRetryUsed = false

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var tokenizer: BertTokenizer? = null

    private var dim: Int = 384
    private var totalRows: Int = 0
    private var embMatrix: MappedByteBuffer? = null
    private var rowWeights: FloatArray = FloatArray(0)
    private var verses: List<IndexedVerse> = emptyList()
    private var verseByRef: Map<String, IndexedVerse> = emptyMap()
    private var reviewedBank: List<ReviewedBankEntry> = emptyList()

    suspend fun initialize() {
        val startedAt = SystemClock.elapsedRealtime()
        var performedAttempt = false
        isInitializing = true
        try {
            withContext(Dispatchers.IO) {
                initializeMutex.withLock {
                    if (ready || initializationAttempted) return@withLock
                    initializationAttempted = true
                    performedAttempt = true
                    try {
                        val appContext = context ?: com.makarios.app.MakariosApp.instance
                        initializeFromAssets(appContext)
                    } catch (failure: Throwable) {
                        handleInitializationFailure(failure, scheduleForegroundRetry = true)
                    }
                }
            }
        } finally {
            isInitializing = false
            if (performedAttempt) PerformanceMetrics.recordEngineInitialization(SystemClock.elapsedRealtime() - startedAt)
        }
    }

    private suspend fun retryAfterForeground() {
        val startedAt = SystemClock.elapsedRealtime()
        var performedAttempt = false
        try {
            withContext(Dispatchers.IO) {
                initializeMutex.withLock {
                    if (ready || !retryPendingForeground || foregroundRetryUsed) return@withLock
                    retryPendingForeground = false
                    foregroundRetryUsed = true
                    initializationAttempted = true
                    performedAttempt = true
                    try {
                        val appContext = context ?: return@withLock
                        initializeFromAssets(appContext)
                    } catch (failure: Throwable) {
                        handleInitializationFailure(failure, scheduleForegroundRetry = false)
                    }
                }
            }
        } finally {
            if (performedAttempt) PerformanceMetrics.recordEngineInitialization(SystemClock.elapsedRealtime() - startedAt)
        }
    }
    /** Test/standalone entry point. Model and embedding streams are copied to files before use. */
    fun initializeFromStreams(
        modelStream: InputStream,
        vocabStream: InputStream,
        indexJsonStream: InputStream,
        embeddingsBinStream: InputStream,
        seedJsonStream: InputStream,
        bankJsonStream: InputStream
    ) {
        ready = false
        initializationAttempted = true
        try {
            val root = context?.filesDir ?: File(System.getProperty("java.io.tmpdir"), "makarios-match-engine").apply { mkdirs() }
            val modelFile = File(root, MODEL_FILE)
            val indexFile = File(root, INDEX_FILE)
            val embeddingsFile = File(root, EMBEDDINGS_FILE)
            copyStreamToFile(modelStream, modelFile)
            copyStreamToFile(indexJsonStream, indexFile)
            copyStreamToFile(embeddingsBinStream, embeddingsFile)
            initializeFromFiles(modelFile, vocabStream, indexFile, embeddingsFile, seedJsonStream, bankJsonStream)
        } catch (failure: Throwable) {
            failure.printStackTrace()
            handleInitializationFailure(failure, scheduleForegroundRetry = context != null)
        }
    }

    private fun initializeFromAssets(ctx: Context) {
        val modelFile = copyAssetIfNeeded(ctx, MODEL_FILE)
        val indexFile = copyAssetIfNeeded(ctx, INDEX_FILE)
        val embeddingsFile = copyAssetIfNeeded(ctx, EMBEDDINGS_FILE)
        ctx.assets.open("vocab.txt").use { vocab ->
            ctx.assets.open("declarations_seed.json").use { seeds ->
                ctx.assets.open("declaration_bank.json").use { bank ->
                    initializeFromFiles(modelFile, vocab, indexFile, embeddingsFile, seeds, bank)
                }
            }
        }
    }

    private fun initializeFromFiles(
        modelFile: File,
        vocabStream: InputStream,
        indexFile: File,
        embeddingsFile: File,
        seedJsonStream: InputStream,
        bankJsonStream: InputStream
    ) {
        ready = false
        ortSession?.close()
        ortSession = null
        tokenizer = null

        // Parse small indices directly from the streams so no second JSON-sized String/JSONObject is made.
        val parsedIndex = readIndex(indexFile)
        dim = parsedIndex.dim
        totalRows = parsedIndex.weights.size
        verses = parsedIndex.verses
        verseByRef = parsedIndex.verseByRef
        rowWeights = parsedIndex.weights

        reviewedBank = readReviewedBank(seedJsonStream, bankJsonStream)
        tokenizer = BertTokenizer.loadFromStream(vocabStream)

        val mapped = FileChannel.open(embeddingsFile.toPath(), StandardOpenOption.READ).use { channel ->
            channel.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size())
        }
        val requiredBytes = totalRows.toLong() * dim.toLong()
        if (mapped.capacity().toLong() < requiredBytes) {
            Timber.e("Embedding map has %d bytes; index requires %d", mapped.capacity(), requiredBytes)
            return
        }
        embMatrix = mapped

        val env = OrtEnvironment.getEnvironment()
        ortEnv = env
        val options = OrtSession.SessionOptions()
        try {
            options.setIntraOpNumThreads(2)
            ortSession = env.createSession(modelFile.absolutePath, options)
        } finally {
            options.close()
        }

        // Keep the reviewed bank available for lexical fallback even if ONNX initialization later fails.
        for (entry in reviewedBank) {
            entry.embedding = computeEmbedding(entry.phrase)
        }
        ready = true
        retryPendingForeground = false
        Timber.i("MatchEngine initialized: %d verses, %d embedding rows", verses.size, totalRows)
    }

    private data class ParsedIndex(
        val dim: Int,
        val verses: List<IndexedVerse>,
        val verseByRef: Map<String, IndexedVerse>,
        val weights: FloatArray
    )

    private fun readIndex(indexFile: File): ParsedIndex {
        val root = org.json.JSONObject(indexFile.readText())
        val parsedDim = root.optInt("dim", 384)
        val versesArray = root.getJSONArray("verses")
        val parsedVerses = ArrayList<IndexedVerse>(versesArray.length())
        val parsedByRef = mutableMapOf<String, IndexedVerse>()
        for (i in 0 until versesArray.length()) {
            val obj = versesArray.getJSONObject(i)
            val v = IndexedVerse(
                id = obj.getInt("id"),
                ref = obj.getString("ref"),
                text = obj.getString("text"),
                book = obj.getString("book"),
                reason = obj.getString("reason")
            )
            parsedVerses.add(v)
            parsedByRef[v.ref] = v
            if (v.ref.startsWith("Psalms ")) {
                parsedByRef["Psalm " + v.ref.removePrefix("Psalms ")] = v
            } else if (v.ref.startsWith("Psalm ")) {
                parsedByRef["Psalms " + v.ref.removePrefix("Psalm ")] = v
            }
        }

        val rowsArray = root.getJSONArray("rows")
        val weights = FloatArray(rowsArray.length())
        for (r in 0 until rowsArray.length()) {
            val rObj = rowsArray.getJSONObject(r)
            val vId = rObj.getInt("v")
            val w = rObj.getDouble("w").toFloat()
            weights[r] = w
            if (vId in parsedVerses.indices) {
                parsedVerses[vId].rowIndices.add(r)
            }
        }
        return ParsedIndex(parsedDim, parsedVerses, parsedByRef, weights)
    }

    private fun readReviewedBank(seedsStream: InputStream, bankStream: InputStream): List<ReviewedBankEntry> {
        val entries = mutableListOf<ReviewedBankEntry>()
        val seedsStr = seedsStream.bufferedReader().use { it.readText() }
        val seedsArr = org.json.JSONArray(seedsStr)
        for (i in 0 until seedsArr.length()) {
            val sObj = seedsArr.getJSONObject(i)
            val decl = sObj.getString("declaration")
            val vArr = sObj.getJSONArray("verses")
            val rList = mutableListOf<String>()
            for (j in 0 until vArr.length()) {
                rList.add(vArr.getJSONObject(j).getString("ref"))
            }
            entries.add(ReviewedBankEntry(decl, rList, "Matches: $decl"))
        }

        val bankStr = bankStream.bufferedReader().use { it.readText() }
        val bankArr = org.json.JSONArray(bankStr)
        for (i in 0 until bankArr.length()) {
            val bObj = bankArr.getJSONObject(i)
            val decl = bObj.getString("declaration")
            val refs = bObj.getJSONArray("references")
            val rList = mutableListOf<String>()
            for (j in 0 until refs.length()) {
                rList.add(refs.getString(j))
            }
            val reason = "Matches: $decl"
            entries.add(ReviewedBankEntry(decl, rList, reason))
            val aliases = bObj.optJSONArray("aliases")
            if (aliases != null) {
                for (k in 0 until aliases.length()) {
                    entries.add(ReviewedBankEntry(aliases.getString(k), rList, reason))
                }
            }
        }
        return entries
    }

    private class MappedBufferInputStream(private val buffer: ByteBuffer) : InputStream() {
        override fun read(): Int = if (buffer.hasRemaining()) buffer.get().toInt() and 0xFF else -1

        override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            if (!buffer.hasRemaining()) return -1
            val count = minOf(length, buffer.remaining())
            buffer.get(bytes, offset, count)
            return count
        }
    }
    private fun copyAssetIfNeeded(ctx: Context, assetName: String): File {
        val output = File(ctx.filesDir, assetName)
        val expectedSize = runCatching { ctx.assets.openFd(assetName).use { it.length } }.getOrNull()
        if (expectedSize != null && expectedSize >= 0L && output.isFile && output.length() == expectedSize) return output
        ctx.assets.open(assetName).use { input -> copyStreamToFile(input, output) }
        return output
    }

    private fun copyStreamToFile(input: InputStream, output: File) {
        output.parentFile?.mkdirs()
        if (output.exists() && output.length() > 0L) {
            return
        }
        val temp = File(output.parentFile ?: output.absoluteFile.parentFile, output.name + ".tmp")
        FileOutputStream(temp).use { out ->
            val buffer = ByteArray(COPY_BUFFER_BYTES)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                out.write(buffer, 0, count)
            }
            out.fd.sync()
        }
        if (output.exists() && !output.delete()) {
            temp.copyTo(output, overwrite = true)
            temp.delete()
        } else if (!temp.renameTo(output)) {
            temp.copyTo(output, overwrite = true)
            temp.delete()
        }
    }

    fun computeEmbedding(text: String): FloatArray? {
        val tok = tokenizer ?: return null
        val session = ortSession ?: return null
        val env = ortEnv ?: return null

        val tokenized = tok.tokenize(text, maxSeqLength = 64)
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
            try {
                @Suppress("UNCHECKED_CAST")
                val lastHidden = outputs.get(0).value as Array<Array<FloatArray>>
                val seqLen = tokenized.inputIds.size
                val pooled = FloatArray(dim)
                var count = 0f
                for (i in 0 until seqLen) {
                    if (tokenized.attentionMask[i] == 1L) {
                        val h = lastHidden[0][i]
                        for (d in 0 until dim) pooled[d] += h[d]
                        count += 1f
                    }
                }
                if (count > 0f) for (d in 0 until dim) pooled[d] /= count
                var sumSq = 0f
                for (d in 0 until dim) sumSq += pooled[d] * pooled[d]
                val norm = sqrt(sumSq).coerceAtLeast(1e-9f)
                for (d in 0 until dim) pooled[d] /= norm
                pooled
            } finally {
                outputs.close()
            }
        } finally {
            inputIdsTensor.close()
            maskTensor.close()
            typeIdsTensor.close()
        }
    }

    private fun expandQuery(text: String): String {
        val lower = text.lowercase().trim()
        var expanded = text
        if ("inside" in lower || "within me" in lower || "in me" in lower) expanded += " in you, indwelling, Christ in you, Spirit within"
        if ("can do anything" in lower || "literally do anything" in lower || "can do all things" in lower) expanded += " all things through Christ"
        if ("never stranded" in lower || "not alone" in lower || "never alone" in lower) expanded += " never leave nor forsake"
        if ("safe" in lower || "hiding" in lower || "refuge" in lower) expanded += " Psalm 91, Colossians 3:3 refuge hiding place"
        return expanded
    }

    suspend fun match(text: String, limit: Int = 3): EngineMatchResult = withContext(Dispatchers.Default) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return@withContext EngineMatchResult(emptyList(), false, "No confident match")

        if (!ready) initialize()
        val rawEmbedding = try { computeEmbedding(trimmed) } catch (failure: Throwable) {
            Timber.e(failure, "MatchEngine embedding failed; using lexical fallback")
            null
        }
        val matrix = embMatrix
        if (!ready || rawEmbedding == null || matrix == null) {
            return@withContext lexicalFallback(trimmed, limit)
        }

        var bestBankScore = -1f
        var bestBankEntry: ReviewedBankEntry? = null
        for (entry in reviewedBank) {
            val bankEmbedding = entry.embedding ?: continue
            var dot = 0f
            for (d in 0 until dim) dot += rawEmbedding[d] * bankEmbedding[d]
            if (dot > bestBankScore) {
                bestBankScore = dot
                bestBankEntry = entry
            }
        }

        if (bestBankScore >= 0.80f && bestBankEntry != null) {
            val curatedMatches = matchesForReferences(bestBankEntry.refs, bestBankScore, limit)
            if (curatedMatches.isNotEmpty()) return@withContext EngineMatchResult(curatedMatches, true, bestBankEntry.reason)
        }

        val expandedText = expandQuery(trimmed)
        val queryEmbedding = if (expandedText == trimmed) rawEmbedding else (computeEmbedding(expandedText) ?: rawEmbedding)
        val rowScores = FloatArray(totalRows)
        for (row in 0 until totalRows) {
            var dot = 0f
            val offset = row * dim
            for (d in 0 until dim) dot += queryEmbedding[d] * (matrix.get(offset + d) / 127f)
            rowScores[row] = dot
        }

        val verseScores = ArrayList<Pair<Float, IndexedVerse>>(verses.size)
        for (verse in verses) {
            var best = -1f
            for (rowIndex in verse.rowIndices) {
                val score = rowScores[rowIndex] * rowWeights[rowIndex]
                if (score > best) best = score
            }
            verseScores.add(Pair(best, verse))
        }
        verseScores.sortByDescending { it.first }

        val topScore = verseScores.firstOrNull()?.first ?: 0f
        if (topScore < 0.42f || verseScores.isEmpty()) {
            return@withContext EngineMatchResult(emptyList(), false, "No confident match")
        }

        val results = mutableListOf<VerseMatch>()
        val seenBooks = mutableSetOf<String>()
        val primaryReason = verseScores[0].second.reason
        for ((index, pair) in verseScores.withIndex()) {
            val score = pair.first
            val verse = pair.second
            if (index == 0) {
                results.add(VerseMatch(verse.ref, ScriptureText.clean(verse.text), true, score))
                seenBooks.add(verse.book)
            } else if (results.size < limit) {
                if (verse.book !in seenBooks) {
                    results.add(VerseMatch(verse.ref, ScriptureText.clean(verse.text), true, score))
                    seenBooks.add(verse.book)
                } else if (results.size == 1 && abs(score - verseScores[0].first) <= 0.02f) {
                    results.add(VerseMatch(verse.ref, ScriptureText.clean(verse.text), true, score))
                }
            }
        }
        EngineMatchResult(results, true, primaryReason)
    }

    private fun lexicalFallback(text: String, limit: Int): EngineMatchResult {
        val queryTokens = ScriptureMatcher.tokens(text)
        if (queryTokens.isNotEmpty()) {
            val bestBank = reviewedBank.maxByOrNull { entry ->
                ScriptureMatcher.cosineSimilarity(queryTokens, ScriptureMatcher.tokens(entry.phrase))
            }
            if (bestBank != null) {
                val score = ScriptureMatcher.cosineSimilarity(queryTokens, ScriptureMatcher.tokens(bestBank.phrase))
                if (score >= LEXICAL_BANK_THRESHOLD) {
                    val bankMatches = matchesForReferences(bestBank.refs, score, limit)
                    if (bankMatches.isNotEmpty()) return EngineMatchResult(bankMatches, true, bestBank.reason)
                }
            }
        }

        val ranked = ScriptureMatcher.match(text, limit = maxOf(limit, 3)).take(limit)
        val matches = ranked.map { result ->
            VerseMatch(result.verse.reference, ScriptureText.clean(result.verse.text), true, result.score)
        }
        val confident = matches.firstOrNull()?.score?.let { it >= 0.35f } == true
        return EngineMatchResult(matches, confident, if (confident) "curated_match" else "No confident match")
    }

    private fun matchesForReferences(refs: List<String>, score: Float, limit: Int): List<VerseMatch> {
        val matches = mutableListOf<VerseMatch>()
        val seenBooks = mutableSetOf<String>()
        for (ref in refs) {
            val indexedVerse = verseByRef[ref]
            val text = indexedVerse?.text
                ?: DeclarableIndex.verses.firstOrNull { it.reference.equals(ref, ignoreCase = true) }?.text
                ?: ScriptureDatabase.verses.firstOrNull { it.reference.equals(ref, ignoreCase = true) }?.text
            val book = indexedVerse?.book
                ?: ref.substringBeforeLast(' ')
            if (text != null && seenBooks.add(book)) {
                matches.add(VerseMatch(ref, ScriptureText.clean(text), true, score))
                if (matches.size >= limit) break
            }
        }
        return matches
    }

    private fun handleInitializationFailure(failure: Throwable, scheduleForegroundRetry: Boolean) {
        ready = false
        try { ortSession?.close() } catch (_: Throwable) { }
        ortSession = null
        retryPendingForeground = scheduleForegroundRetry && !foregroundRetryUsed
        Timber.e(failure, "MatchEngine initialization failed; Declare will use reviewed and lexical matching")
    }

    companion object {
        private const val MODEL_FILE = "minilm_l12_quantized.onnx"
        private const val EMBEDDINGS_FILE = "embeddings_i8.bin"
        private const val INDEX_FILE = "index.json"
        private const val COPY_BUFFER_BYTES = 8 * 1024
        private const val LEXICAL_BANK_THRESHOLD = 0.72f

        @Volatile private var instance: MatchEngine? = null

        fun getInstance(context: Context): MatchEngine =
            instance ?: synchronized(this) {
                instance ?: MatchEngine(context.applicationContext).also { instance = it }
            }

        fun createStandalone(): MatchEngine = MatchEngine(null)

        suspend fun retryPendingInstanceOnForeground() {
            instance?.retryAfterForeground()
        }
    }
}
