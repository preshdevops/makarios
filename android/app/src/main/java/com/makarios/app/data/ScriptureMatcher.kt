package com.makarios.app.data

/**
 * Offline scripture matching engine.
 *
 * Analyzes the user's declaration text and returns ranked Bible verses
 * from [ScriptureDatabase] based on keyword overlap, thematic relevance,
 * and tone affinity. Fully deterministic, no networking required.
 */
object ScriptureMatcher {

    data class MatchResult(
        val verse: ScriptureVerse,
        val score: Float
    )

    // Common English stop words filtered from tokenization
    private val stopWords = setOf(
        "a", "the", "is", "in", "and",
        "to", "of", "that", "not", "for", "with", "by", "on",
        "it", "this", "but", "have", "do", "will", "be", "are",
        "was", "has", "an", "or", "so", "if", "at", "from",
        "can", "all", "into", "because", "been", "than",
        "its", "who", "what", "when", "how", "just", "also",
        "about", "more", "any"
    )

    /**
     * Matches the user's declaration against the scripture corpus.
     *
     * @param declaration  The user's typed affirmation text
     * @param tone         The user's selected tone (null = no preference)
     * @param limit        Maximum results to return (default 8)
     * @return Ranked list of [MatchResult], highest score first
     */
    fun match(
        declaration: String,
        tone: AffirmationTone? = null,
        limit: Int = 8
    ): List<MatchResult> {
        val tokens = tokenize(declaration)

        val scored = ScriptureDatabase.verses
            .map { verse -> MatchResult(verse, score(tokens, verse, tone)) }
            .filter { it.score > 0f }
            .sortedByDescending { it.score }
            .take(limit)

        // Fallback: if nothing matched, return general identity verses
        return scored.ifEmpty {
            ScriptureDatabase.verses
                .filter { "identity" in it.themes }
                .take(3)
                .map { MatchResult(it, 1f) }
                .ifEmpty {
                    // Ultimate fallback: first 3 verses in the database
                    ScriptureDatabase.verses.take(3).map { MatchResult(it, 1f) }
                }
        }
    }

    /**
     * Tokenizes declaration text into a set of meaningful lowercase words.
     * Strips punctuation, filters stop words and words < 2 characters.
     */
    private fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("[^a-z\\s]"), "")
            .split("\\s+".toRegex())
            .filter { it.length >= 2 && it !in stopWords }
            .toSet()
    }

    /**
     * Scores a single verse against the user's declaration tokens.
     *
     * Formula:
     *   score = (keywordHits * 10) + (themeHits * 5) + (toneBonus * 3)
     *
     * Keyword matching uses substring containment in both directions
     * to catch partial stems (e.g. "fearful" matches keyword "fear").
     */
    private fun score(
        tokens: Set<String>,
        verse: ScriptureVerse,
        selectedTone: AffirmationTone?
    ): Float {
        // 1. Keyword overlap — primary signal
        val keywordHits = tokens.count { token ->
            verse.keywords.any { keyword ->
                keyword.contains(token) || token.contains(keyword)
            }
        }

        // 2. Theme match — does the declaration mention a theme?
        val themeHits = tokens.count { token ->
            verse.themes.any { theme ->
                theme.contains(token) || token.contains(theme)
            }
        }

        // 3. Tone affinity bonus
        val toneBonus = if (selectedTone != null && verse.toneAffinity == selectedTone) 1 else 0

        return (keywordHits * 10f) + (themeHits * 5f) + (toneBonus * 3f)
    }
}
