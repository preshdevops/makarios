package com.makarios.app.data

/** Curated, declarable retrieval index with narrative filtering and bank integration. */
object DeclarableIndex {
    private val blocked = setOf(
        "curse", "cursed", "judgment", "judgement", "wrath", "destroy",
        "destruction", "wicked", "evil", "bloodshed", "vengeance"
    )

    private val narrativeOffenders = setOf(
        "John 7:4", "Mark 14:7", "Matthew 26:11", "Mark 14:6",
        "Matthew 26:10", "Matthew 27:1", "John 18:1", "Luke 22:1"
    )

    private val narrativeStarters = listOf(
        "and he said", "and she said", "now when", "then he said",
        "after this", "when jesus saw", "there was a", "and they said", "then they said"
    )

    val verses: List<ScriptureVerse> by lazy {
        ScriptureDatabase.verses.filter { verse ->
            if (verse.reference in narrativeOffenders) return@filter false
            val lowerText = verse.text.lowercase().trim()
            if (narrativeStarters.any { lowerText.startsWith(it) }) return@filter false
            val combined = "${verse.reference} ${verse.text} ${verse.themes.joinToString(" ")} ${verse.keywords.joinToString(" ")}".lowercase()
            blocked.none { it in combined } || verse.reference == "Philippians 4:13" || verse.reference == "Deuteronomy 28:13"
        }
    }
}

object DeclarationBank {
    private val entries: List<BankEntry> get() = DeclarationBankData.entries

    private val tokenizedEntries: List<TokenizedBankEntry> by lazy {
        entries.map { entry ->
            val allPhrases = listOf(entry.declaration) + entry.aliases
            val tokenSets = allPhrases.map { ScriptureMatcher.tokens(it, dropFillers = true) }
            val normalizedPhrases = allPhrases.map { normalize(it) }
            TokenizedBankEntry(entry, normalizedPhrases, tokenSets, entry.references)
        }
    }

    data class TokenizedBankEntry(
        val entry: BankEntry,
        val normalizedPhrases: List<String>,
        val tokenSets: List<Set<String>>,
        val references: List<String>
    )

    private fun normalize(text: String): String =
        text.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()

    fun match(queryText: String, queryTokens: Set<String>): Pair<List<String>, Float>? {
        val normQuery = normalize(queryText)
        val filteredWords = normQuery.split(" ").filter { it !in ScriptureMatcher.FILLER_WORDS && it.isNotBlank() }
        val filteredQuery = filteredWords.joinToString(" ")

        var bestEntry: TokenizedBankEntry? = null
        var bestCosine = 0f

        for (bankEntry in tokenizedEntries) {
            // 1. Exact phrase or stripped phrase match
            for (p in bankEntry.normalizedPhrases) {
                val pFiltered = p.split(" ").filter { it !in ScriptureMatcher.FILLER_WORDS && it.isNotBlank() }.joinToString(" ")
                if (normQuery == p || filteredQuery == pFiltered ||
                    (pFiltered.isNotBlank() && filteredQuery.contains(pFiltered)) ||
                    (filteredQuery.isNotBlank() && pFiltered.contains(filteredQuery))
                ) {
                    return Pair(bankEntry.references, 1.0f)
                }
            }

            // 2. Cosine similarity against bank token sets
            for (tSet in bankEntry.tokenSets) {
                val cos = ScriptureMatcher.cosineSimilarity(queryTokens, tSet)
                if (cos > bestCosine) {
                    bestCosine = cos
                    if (cos >= 0.80f) {
                        bestEntry = bankEntry
                    }
                }
            }
        }

        return if (bestEntry != null && bestCosine >= 0.80f) {
            Pair(bestEntry.references, bestCosine)
        } else null
    }

    fun referencesFor(text: String): List<String> {
        val qTokens = ScriptureMatcher.tokens(text, dropFillers = true)
        return match(text, qTokens)?.first.orEmpty()
    }
}



