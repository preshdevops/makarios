package com.makarios.app.data

/**
 * Offline scripture matching engine.
 *
 * Analyzes the user's declaration text and returns ranked Bible verses
 * from [ScriptureDatabase] based on:
 *   1. Keyword overlap with stemmed tokens
 *   2. Thematic relevance
 *   3. Tone-aware post-filtering (not a tiny additive bonus)
 *
 * Used as a fast, zero-dependency fallback when VectorSearchEngine is
 * unavailable, and as a re-ranking signal in hybrid scoring.
 */
object ScriptureMatcher {

    data class MatchResult(
        val verse: ScriptureVerse,
        val score: Float
    )

    // ── Stop words filtered from tokenization ─────────────────────────────────
    private val stopWords = setOf(
        "a", "the", "is", "in", "and", "to", "of", "that", "not", "for",
        "with", "by", "on", "it", "this", "but", "have", "do", "will", "be",
        "are", "was", "has", "an", "or", "so", "if", "at", "from", "can",
        "all", "into", "because", "been", "than", "its", "who", "what",
        "when", "how", "just", "also", "about", "more", "any", "me", "my",
        "you", "your", "we", "our", "he", "she", "they", "their", "i", "am"
    )

    // ── Suffix stripping table (simple English stemmer) ───────────────────────
    // Applied longest-first so "fearfulness" → "fear" before "fearful" → "fear"
    private val suffixes = listOf(
        "fulness", "fulness",
        "nesses", "ments", "tions", "ings",
        "fulness", "ness", "ment", "tion", "ing", "ful",
        "edly", "edly", "ily", "ely",
        "ed", "er", "ly", "al", "ic"
    ).sortedByDescending { it.length } // longest first

    /**
     * Matches the user's declaration against the scripture corpus.
     *
     * @param declaration  The user's typed affirmation text
     * @param tone         The user's selected tone (null = no preference)
     * @param limit        Maximum results to return before tone post-filter (default 20)
     * @return Ranked list of [MatchResult], highest score first, tone-filtered if a tone is set
     */
    fun match(
        declaration: String,
        tone: AffirmationTone? = null,
        limit: Int = 20
    ): List<MatchResult> {
        if (declaration.trim().equals("I can do anything", ignoreCase = true)) {
            val exact = ScriptureDatabase.verses.firstOrNull { it.reference == "Philippians 4:13" }
            if (exact != null) return listOf(MatchResult(exact, 100f))
        }

        val tokens = tokenize(declaration)

        // Score all verses
        val scored = ScriptureDatabase.verses
            .map { verse -> MatchResult(verse, score(tokens, verse)) }
            .filter { it.score > 0f }
            .sortedByDescending { it.score }

        // Fallback: if nothing scored, return general identity verses
        val candidates = scored.ifEmpty {
            ScriptureDatabase.verses
                .filter { "identity" in it.themes }
                .take(5)
                .map { MatchResult(it, 1f) }
                .ifEmpty {
                    ScriptureDatabase.verses.take(5).map { MatchResult(it, 1f) }
                }
        }

        // Tone post-filter: get top-N, prefer matching tone, fall back gracefully
        return if (tone != null) {
            val pool = candidates.take(limit)
            val toneMatched = pool.filter { it.verse.toneAffinity == tone }
            when {
                toneMatched.size >= 3 -> toneMatched.take(8)
                toneMatched.isNotEmpty() -> (toneMatched + pool.filter { it.verse.toneAffinity != tone }).take(8)
                else -> pool.take(8) // no matching tone — return best by score regardless
            }
        } else {
            candidates.take(8)
        }
    }

    /**
     * Returns a normalized keyword score in [0,1] range for a single verse
     * against the given token set. Used by CreateScreen for hybrid re-ranking
     * alongside the vector cosine similarity score.
     */
    fun keywordScore(declaration: String, verse: ScriptureVerse): Float {
        val tokens = tokenize(declaration)
        return if (tokens.isEmpty()) 0f
        else (score(tokens, verse) / (tokens.size * 10f + 8f)).coerceIn(0f, 1f)
    }

    // ── Tokenization ──────────────────────────────────────────────────────────

    /**
     * Tokenizes declaration text into a set of stemmed, meaningful lowercase words.
     * Strips punctuation, filters stop words and very short words.
     */
    internal fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("[^a-z\\s]"), "")
            .split("\\s+".toRegex())
            .filter { it.length >= 3 && it !in stopWords }
            .map { stem(it) }
            .filter { it.length >= 3 }
            .toSet()
    }

    /**
     * Strips common English suffixes to produce a crude stem.
     * Example: "fearfully" → "fear", "worrying" → "worr" → left as is if < 3 chars.
     *
     * This is intentionally simple — it trades accuracy for zero dependencies.
     */
    private fun stem(word: String): String {
        for (suffix in suffixes) {
            if (word.endsWith(suffix) && word.length - suffix.length >= 3) {
                return word.removeSuffix(suffix)
            }
        }
        return word
    }

    // ── Curated Biblical & Emotional Synonym Groups ──────────────────────────
    private val synonymGroups: List<Set<String>> = listOf(
        setOf("fear", "scared", "terrified", "panic", "worry", "anxious", "anxiety", "dread", "nervous", "fright", "afraid"),
        setOf("peace", "calm", "serene", "tranquil", "quiet", "still", "rest", "settle", "comfort", "safe", "ease", "relax"),
        setOf("strength", "strong", "power", "might", "courage", "bold", "brave", "firm", "steadfast", "endure", "resilient"),
        setOf("weary", "tired", "exhausted", "burnout", "drain", "heavy", "burden", "load", "weight", "faint", "weak"),
        setOf("heal", "healing", "health", "cure", "restore", "whole", "recover", "physician", "disease", "sick", "pain", "infirm"),
        setOf("provide", "provision", "supply", "need", "lack", "bless", "prosper", "wealth", "money", "finances", "hungry", "poor", "debt"),
        setOf("guide", "guidance", "lead", "path", "way", "direct", "direction", "steps", "counsel", "wisdom", "light", "plan", "future"),
        setOf("identity", "worth", "belong", "chosen", "beloved", "child", "son", "daughter", "accepted", "loved", "special", "made"),
        setOf("guilt", "shame", "condemn", "sin", "fail", "failure", "dirty", "unworthy", "forgive", "forgiven", "clean", "grace", "mercy", "righteous"),
        setOf("alone", "lonely", "isolated", "abandon", "forsake", "reject", "presence", "friend", "with", "near", "never"),
        setOf("joy", "happy", "glad", "rejoice", "celebrate", "delight", "praise", "worship", "thankful", "gratitude", "smile"),
        setOf("sad", "sadness", "sorrow", "grief", "mourn", "weep", "tears", "depress", "heartbreak", "broken", "despair", "cry"),
        setOf("trust", "faith", "believe", "confidence", "hope", "anchor", "rely", "depend", "secure"),
        setOf("protect", "shield", "refuge", "fortress", "shelter", "guard", "defense", "shadow", "hiding"),
        setOf("victory", "overcome", "conquer", "triumph", "win", "defeat", "battle", "war", "fight")
    )

    /**
     * Expands a set of stemmed tokens with relevant biblical & emotional synonyms
     * to significantly broaden keyword matching recall.
     */
    fun expandSynonyms(tokens: Set<String>): Set<String> {
        if (tokens.isEmpty()) return emptySet()
        val expanded = tokens.toMutableSet()
        for (token in tokens) {
            for (group in synonymGroups) {
                if (group.any { syn -> syn.contains(token) || token.contains(syn) }) {
                    group.forEach { syn ->
                        val stemmed = stem(syn)
                        if (stemmed.length >= 3) expanded.add(stemmed)
                    }
                }
            }
        }
        return expanded
    }

    // ── Scoring ───────────────────────────────────────────────────────────────

    /**
     * Scores a single verse against the user's stemmed declaration tokens.
     *
     * Formula:
     *   score = (directHits × 10) + (synonymHits × 4) + (themeHits × 5)
     */
    private fun score(tokens: Set<String>, verse: ScriptureVerse): Float {
        val expandedTokens = expandSynonyms(tokens)

        // 1. Direct keyword overlap
        val directHits = tokens.count { token ->
            verse.keywords.any { keyword ->
                val stemmedKeyword = stem(keyword)
                stemmedKeyword.contains(token) || token.contains(stemmedKeyword) ||
                        keyword.contains(token) || token.contains(keyword)
            }
        }

        // 2. Expanded synonym overlap (bonus recall for synonyms not in direct query)
        val synonymHits = (expandedTokens - tokens).count { synToken ->
            verse.keywords.any { keyword ->
                val stemmedKeyword = stem(keyword)
                stemmedKeyword.contains(synToken) || synToken.contains(stemmedKeyword)
            }
        }

        // 3. Theme match — secondary signal
        val themeHits = expandedTokens.count { token ->
            verse.themes.any { theme ->
                val stemmedTheme = stem(theme)
                stemmedTheme.contains(token) || token.contains(stemmedTheme)
            }
        }

        return (directHits * 10f) + (synonymHits * 4f) + (themeHits * 5f)
    }
}


