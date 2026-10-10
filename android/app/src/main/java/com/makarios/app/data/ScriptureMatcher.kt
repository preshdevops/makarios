package com.makarios.app.data

import com.makarios.app.data.bible.ScriptureText
import kotlin.math.sqrt

/** Fast curated stopgap engine with stemmed tokens, bank cosine matching, and narrative exclusion. */
object ScriptureMatcher {
    data class MatchResult(val verse: ScriptureVerse, val score: Float)

    val FILLER_WORDS = setOf(
        "literally", "really", "just", "very", "always", "never", "actually",
        "basically", "totally", "truly", "simply", "definitely", "completely", "absolutely"
    )

    val STOP_WORDS = setOf(
        "the", "and", "for", "that", "with", "this", "from", "your", "you", "are", "can", "not",
        "all", "who", "will", "have", "has", "was", "but", "into", "through", "about", "above",
        "across", "after", "again", "against", "along", "also", "among", "any", "because", "been",
        "before", "being", "below", "between", "both", "could", "did", "does", "doing", "down",
        "during", "each", "few", "further", "had", "here", "him", "his", "how", "its", "more",
        "most", "myself", "nor", "off", "once", "only", "other", "our", "ours", "out", "over",
        "own", "same", "she", "should", "some", "such", "than", "them", "then", "there", "these",
        "they", "those", "too", "under", "until", "upon", "what", "when", "where", "which",
        "while", "whom", "why", "would"
    )

    fun stem(word: String): String {
        val w = word.lowercase()
        return when {
            w.endsWith("ing") && w.length > 5 -> w.removeSuffix("ing")
            w.endsWith("ed") && w.length > 4 -> w.removeSuffix("ed")
            w.endsWith("es") && w.length > 4 -> w.removeSuffix("es")
            w.endsWith("s") && w.length > 3 && !w.endsWith("ss") -> w.removeSuffix("s")
            w.endsWith("ly") && w.length > 4 -> w.removeSuffix("ly")
            w.endsWith("tion") && w.length > 6 -> w.removeSuffix("tion")
            w.endsWith("ment") && w.length > 6 -> w.removeSuffix("ment")
            w.endsWith("ness") && w.length > 6 -> w.removeSuffix("ness")
            else -> w
        }
    }

    internal fun tokens(text: String, dropFillers: Boolean = true): Set<String> {
        val clean = ScriptureText.clean(text).lowercase().replace(Regex("[^a-z0-9 ]"), " ")
        return clean.split(Regex("\\s+"))
            .filter { it.length >= 2 && it !in STOP_WORDS && (!dropFillers || it !in FILLER_WORDS) }
            .map { stem(it) }
            .toSet()
    }

    fun cosineSimilarity(tokens1: Set<String>, tokens2: Set<String>): Float {
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0f
        val intersection = tokens1.count { it in tokens2 }
        val denom = sqrt((tokens1.size * tokens2.size).toDouble()).toFloat()
        return if (denom > 0f) (intersection / denom).coerceIn(0f, 1f) else 0f
    }

    fun match(declaration: String, tone: AffirmationTone? = null, limit: Int = 20): List<MatchResult> {
        val cleaned = ScriptureText.clean(declaration)
        if (cleaned.isBlank()) return emptyList()

        val query = tokens(cleaned, dropFillers = true)
        if (query.isEmpty()) return emptyList()

        val results = mutableListOf<MatchResult>()
        val seen = mutableSetOf<String>()

        // 1. Bank match (Exact or Cosine >= 0.80)
        val bankMatch = DeclarationBank.match(cleaned, query)
        if (bankMatch != null) {
            val (refs, cos) = bankMatch
            val baseScore = (cos * 0.95f).coerceAtLeast(0.90f)
            refs.forEachIndexed { idx, ref ->
                val v = DeclarableIndex.verses.firstOrNull { it.reference.equals(ref, ignoreCase = true) }
                    ?: ScriptureDatabase.verses.firstOrNull { it.reference.equals(ref, ignoreCase = true) }
                if (v != null && seen.add(v.reference)) {
                    results.add(MatchResult(v, (baseScore - idx * 0.02f).coerceAtLeast(0.85f)))
                }
            }
        }

        // 2. Whole-token scoring across declarable index
        val scoredIndex = DeclarableIndex.verses.asSequence()
            .filter { it.reference !in seen }
            .map { verse -> MatchResult(verse, score(query, verse, tone)) }
            .filter { it.score >= 0.35f } // Threshold: below 0.35 is not confident
            .sortedByDescending { it.score }
            .toList()

        for (m in scoredIndex) {
            if (seen.add(m.verse.reference)) {
                results.add(m)
            }
        }

        return results.take(limit)
    }

    private fun score(query: Set<String>, verse: ScriptureVerse, tone: AffirmationTone?): Float {
        val verseTextTokens = tokens(verse.text, dropFillers = false)
        val textCosine = cosineSimilarity(query, verseTextTokens)

        // Whole-token match against stemmed keywords
        val verseKwTokens = verse.keywords.map { stem(it.lowercase()) }.filter { it !in STOP_WORDS && it !in FILLER_WORDS }.toSet()
        val kwOverlap = if (verseKwTokens.isNotEmpty()) {
            query.count { it in verseKwTokens }.toFloat() / maxOf(query.size, 1)
        } else 0f

        // Keyword weight is 0.1 and ONLY applied when kwOverlap > 0.5f
        val kwBonus = if (kwOverlap > 0.5f) kwOverlap * 0.10f else 0f

        val toneBonus = if (tone != null && verse.toneAffinity == tone) 0.05f else 0f
        val qualityBonus = verseQuality(verse) * 0.05f

        val total = (textCosine * 0.80f + kwBonus + toneBonus + qualityBonus).coerceIn(0f, 1f)
        return total
    }

    private fun verseQuality(verse: ScriptureVerse): Float =
        if (verse.themes.any { it in setOf("identity", "peace", "strength", "hope", "love", "rest", "protection") }) 1f else 0.5f

    fun expandSynonyms(tokens: Set<String>): Set<String> = tokens
    fun keywordScore(declaration: String, verse: ScriptureVerse): Float = score(tokens(declaration), verse, null)
}
