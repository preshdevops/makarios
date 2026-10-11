package com.makarios.app.data

import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class EngineEvaluationTest {

    companion object {
        private lateinit var engine: MatchEngine
        private lateinit var evalCases: JSONArray

        private fun findFile(pathVariants: List<String>): File {
            for (p in pathVariants) {
                val f = File(p)
                if (f.exists()) return f
            }
            error("None of the file paths exist: $pathVariants")
        }

        @JvmStatic
        @BeforeClass
        fun setup() {
            val modelFile = findFile(listOf(
                "src/main/assets/minilm_l12_quantized.onnx",
                "app/src/main/assets/minilm_l12_quantized.onnx",
                "android/app/src/main/assets/minilm_l12_quantized.onnx"
            ))
            val vocabFile = findFile(listOf(
                "src/main/assets/vocab.txt",
                "app/src/main/assets/vocab.txt",
                "android/app/src/main/assets/vocab.txt"
            ))
            val indexFile = findFile(listOf(
                "src/main/assets/index.json",
                "app/src/main/assets/index.json",
                "android/app/src/main/assets/index.json"
            ))
            val embFile = findFile(listOf(
                "src/main/assets/embeddings_i8.bin",
                "app/src/main/assets/embeddings_i8.bin",
                "android/app/src/main/assets/embeddings_i8.bin"
            ))
            val seedFile = findFile(listOf(
                "src/main/assets/declarations_seed.json",
                "app/src/main/assets/declarations_seed.json",
                "android/app/src/main/assets/declarations_seed.json"
            ))
            val bankFile = findFile(listOf(
                "src/main/assets/declaration_bank.json",
                "app/src/main/assets/declaration_bank.json",
                "android/app/src/main/assets/declaration_bank.json"
            ))
            val evalFile = findFile(listOf(
                "src/test/resources/eval_declarations.json",
                "app/src/test/resources/eval_declarations.json",
                "android/app/src/test/resources/eval_declarations.json"
            ))

            engine = MatchEngine.createStandalone()
            modelFile.inputStream().use { mStream ->
                vocabFile.inputStream().use { vocStream ->
                    indexFile.inputStream().use { idxStream ->
                        embFile.inputStream().use { embStream ->
                            seedFile.inputStream().use { sStream ->
                                bankFile.inputStream().use { bStream ->
                                    engine.initializeFromStreams(
                                        mStream,
                                        vocStream,
                                        idxStream,
                                        embStream,
                                        sStream,
                                        bStream
                                    )
                                }
                            }
                        }
                    }
                }
            }

            evalCases = JSONArray(evalFile.readText())
        }

        private fun normalizeRef(r: String): String {
            val trimmed = r.trim()
            return if (trimmed.startsWith("Psalms ")) "Psalm " + trimmed.removePrefix("Psalms ") else trimmed
        }

        private fun refMatches(got: String, exp: String): Boolean {
            val g = normalizeRef(got)
            val e = normalizeRef(exp)
            if (g == e) return true

            val mG = Regex("""^(.*)\s+(\d+):(\d+)-(\d+)$""").matchEntire(g)
            val mE = Regex("""^(.*)\s+(\d+):(\d+)$""").matchEntire(e)
            if (mG != null && mE != null && mG.groupValues[1] == mE.groupValues[1] && mG.groupValues[2] == mE.groupValues[2]) {
                val start = mG.groupValues[3].toInt()
                val end = mG.groupValues[4].toInt()
                val target = mE.groupValues[3].toInt()
                if (target in start..end) return true
            }

            val mE2 = Regex("""^(.*)\s+(\d+):(\d+)-(\d+)$""").matchEntire(e)
            val mG2 = Regex("""^(.*)\s+(\d+):(\d+)$""").matchEntire(g)
            if (mE2 != null && mG2 != null && mE2.groupValues[1] == mG2.groupValues[1] && mE2.groupValues[2] == mG2.groupValues[2]) {
                val start = mE2.groupValues[3].toInt()
                val end = mE2.groupValues[4].toInt()
                val target = mG2.groupValues[3].toInt()
                if (target in start..end) return true
            }
            return false
        }
    }

    @Test
    fun testSpecificQueriesIdiomMapping() = runBlocking {
        // "God is on my inside": Colossians 1:27, 1 John 4:4, Philippians 4:13, Galatians 2:20, 2 Corinthians 6:16
        val res = engine.match("God is on my inside", limit = 3)
        assertTrue("Expected confident match for 'God is on my inside'", res.confident)
        val refs = res.top.map { it.reference }
        val expectedPool = listOf("Colossians 1:27", "1 John 4:4", "Philippians 4:13", "Galatians 2:20", "2 Corinthians 6:16")
        val hasMatch = refs.any { got -> expectedPool.any { exp -> refMatches(got, exp) } }
        assertTrue("Expected one of $expectedPool, got $refs", hasMatch)

        // "I can literally do anything"
        val res2 = engine.match("I can literally do anything", limit = 3)
        assertTrue(res2.confident)
        assertTrue(res2.top.any { refMatches(it.reference, "Philippians 4:13") })

        // "I am never stranded"
        val res3 = engine.match("I am never stranded", limit = 3)
        assertTrue(res3.confident)
        assertTrue(res3.top.any { refMatches(it.reference, "Deuteronomy 31:6") || refMatches(it.reference, "Hebrews 13:5") })
    }

    @Test
    fun testFullEvaluationHarness() = runBlocking {
        var userSeedsTotal = 0
        var userSeedsTop1 = 0
        var userSeedsTop3 = 0

        var inDistTotal = 0
        var inDistTop1 = 0
        var inDistTop3 = 0

        var oodTotal = 0
        var oodCorrect = 0

        val totalCases = evalCases.length()
        assertTrue("Harness must evaluate at least 400 cases, found $totalCases", totalCases >= 400)

        // Warm up search
        engine.match("warm up search query", limit = 3)

        val startTime = System.currentTimeMillis()

        for (i in 0 until totalCases) {
            val obj = evalCases.getJSONObject(i)
            val query = obj.getString("query")
            val cat = obj.getString("category")
            val shouldMatch = obj.getBoolean("should_match")
            val expTop1 = obj.optString("expected_top1", "")
            val expTop3Arr = obj.optJSONArray("expected_top3") ?: JSONArray()
            val expTop3 = mutableListOf<String>()
            for (j in 0 until expTop3Arr.length()) {
                expTop3.add(expTop3Arr.getString(j))
            }

            val result = engine.match(query, limit = 3)
            val topMatches = result.top.map { it.reference }

            if (!shouldMatch) {
                oodTotal++
                if (!result.confident || topMatches.isEmpty()) {
                    oodCorrect++
                }
                continue
            }

            inDistTotal++
            val isTop1 = topMatches.isNotEmpty() && refMatches(topMatches[0], expTop1)
            val isTop3 = topMatches.any { got -> expTop3.any { exp -> refMatches(got, exp) } }

            if (isTop1) inDistTop1++
            if (isTop3) inDistTop3++

            if (cat == "user_seeds") {
                userSeedsTotal++
                if (isTop1) userSeedsTop1++
                if (isTop3) userSeedsTop3++
            }
        }

        val elapsedMs = System.currentTimeMillis() - startTime
        val avgLatencyMs = elapsedMs.toDouble() / totalCases

        val userSeedsTop1Pct = (userSeedsTop1.toDouble() / userSeedsTotal) * 100.0
        val userSeedsTop3Pct = (userSeedsTop3.toDouble() / userSeedsTotal) * 100.0
        val inDistTop3Pct = (inDistTop3.toDouble() / inDistTotal) * 100.0
        val oodPrecisionPct = if (oodTotal > 0) (oodCorrect.toDouble() / oodTotal) * 100.0 else 100.0

        println("\n================ JVM EVALUATION HARNESS RESULTS ================")
        println("Total Cases Evaluated: $totalCases in ${elapsedMs}ms (${String.format("%.2f", avgLatencyMs)} ms/query)")
        println("1. User 28 Seed Declarations:")
        println("   Top-1: $userSeedsTop1/$userSeedsTotal (${String.format("%.1f", userSeedsTop1Pct)}%) [Min: 24/28 (85.7%)]")
        println("   Top-3: $userSeedsTop3/$userSeedsTotal (${String.format("%.1f", userSeedsTop3Pct)}%) [Required: 28/28 (100%)]")
        println("2. In-Distribution Set ($inDistTotal queries):")
        println("   Top-1: $inDistTop1/$inDistTotal (${String.format("%.1f", (inDistTop1.toDouble()/inDistTotal)*100.0)}%)")
        println("   Top-3: $inDistTop3/$inDistTotal (${String.format("%.1f", inDistTop3Pct)}%) [Min: 95.0%]")
        println("3. Out-Of-Distribution / Negative Set ($oodTotal queries):")
        println("   No-match Precision: $oodCorrect/$oodTotal (${String.format("%.1f", oodPrecisionPct)}%)")
        println("4. Average Search Latency: ${String.format("%.2f", avgLatencyMs)} ms (< 150 ms required)")
        println("=================================================================\n")

        // Rule assertions:
        assertEquals("User 28 seeds must be 28/28 in Top-3", 28, userSeedsTop3)
        assertTrue("User 28 seeds Top-1 must be at least 24/28 (got $userSeedsTop1)", userSeedsTop1 >= 24)
        assertTrue("In-distribution Top-3 must be >= 95.0% (got ${String.format("%.1f", inDistTop3Pct)}%)", inDistTop3Pct >= 95.0)
        assertTrue("Average latency must be under 150ms (got $avgLatencyMs ms)", avgLatencyMs < 150.0)
    }
}
