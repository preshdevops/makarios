package com.makarios.app.data

import com.makarios.app.data.bible.ScriptureText
import kotlin.math.max

/** Fast curated fallback used by Declare and by MatchEngine when model assets are unavailable. */
object ScriptureMatcher {
    data class MatchResult(val verse: ScriptureVerse, val score: Float)

    fun match(declaration: String, tone: AffirmationTone? = null, limit: Int = 20): List<MatchResult> {
        val cleaned = ScriptureText.clean(declaration)
        if (cleaned.isBlank()) return emptyList()
        val bankRefs = DeclarationBank.referencesFor(cleaned)
        val bankMatches = bankRefs.mapNotNull { ref -> DeclarableIndex.verses.firstOrNull { it.reference.equals(ref, true) } }.mapIndexed { index, verse -> MatchResult(verse, 1f - index * .01f) }
        if (bankMatches.isNotEmpty()) return bankMatches.take(limit)
        val query = tokens(cleaned)
        if (query.size < 2) return emptyList()
        return DeclarableIndex.verses.asSequence()
            .map { verse -> MatchResult(verse, score(query, verse, tone)) }
            .filter { it.score >= .18f }
            .sortedByDescending { it.score }
            .distinctBy { it.verse.reference }
            .take(limit)
            .toList()
    }

    internal fun tokens(text: String): Set<String> = ScriptureText.clean(text).lowercase().replace(Regex("[^a-z0-9 ]"), " ").split(Regex("\\s+")).filter { it.length >= 3 && it !in STOP_WORDS }.toSet()
    private fun score(query: Set<String>, verse: ScriptureVerse, tone: AffirmationTone?): Float {
        val verseTokens = tokens("${verse.text} ${verse.keywords.joinToString(" ")} ${verse.themes.joinToString(" ")}")
        val overlap = query.count { it in verseTokens }.toFloat() / max(query.size, 1)
        val theme = query.count { token -> verse.themes.any { it.contains(token) || token.contains(it) } }.toFloat() / max(query.size, 1)
        val toneBonus = if (tone != null && verse.toneAffinity == tone) .08f else 0f
        return (overlap * .72f + theme * .20f + toneBonus + verseQuality(verse) * .08f).coerceIn(0f, 1f)
    }
    private fun verseQuality(verse: ScriptureVerse): Float = if (verse.themes.any { it in setOf("identity", "peace", "strength", "hope", "love", "rest", "protection") }) 1f else .5f
    private val STOP_WORDS = setOf("the", "and", "for", "that", "with", "this", "from", "your", "you", "are", "can", "not", "all", "who", "will", "have", "has", "was", "but", "into", "through")

    fun expandSynonyms(tokens: Set<String>): Set<String> = tokens
    fun keywordScore(declaration: String, verse: ScriptureVerse): Float = score(tokens(declaration), verse, null)
}
