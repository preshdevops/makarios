package com.makarios.app.util

/**
 * Pronoun Rewriter utility for transforming first-person declarations
 * ("I am not stranded", "My peace is deep") into second-person gifts
 * ("You are not stranded", "Your peace is deep") for the "For you" share style.
 */
object PronounRewriter {

    private val compoundRules: List<Pair<Regex, String>> = listOf(
        Regex("(?i)\\bI\\s+am\\b") to "you are",
        Regex("(?i)\\bI['’]m\\b") to "you're",
        Regex("(?i)\\bI\\s+was\\b") to "you were",
        Regex("(?i)\\bI\\s+have\\b") to "you have",
        Regex("(?i)\\bI['’]ve\\b") to "you've",
        Regex("(?i)\\bI\\s+will\\b") to "you will",
        Regex("(?i)\\bI['’]ll\\b") to "you'll",
        Regex("(?i)\\bI\\s+shall\\b") to "you shall",
        Regex("(?i)\\bmyself\\b") to "yourself",
        Regex("(?i)\\bmine\\b") to "yours"
    )

    private val wordRules: List<Pair<Regex, String>> = listOf(
        Regex("(?i)\\bmy\\b") to "your",
        Regex("(?i)\\bme\\b") to "you",
        Regex("\\bI\\b") to "you"
    )

    /**
     * Rewrites first-person statements into second-person statements,
     * carefully preserving capitalization and punctuation.
     */
    fun rewrite(input: String): String {
        if (input.isBlank()) return input

        var result = input

        // 1. Process compound rules first
        for ((regex, replacement) in compoundRules) {
            result = regex.replace(result) { matchResult ->
                matchCase(matchResult.value, replacement)
            }
        }

        // 2. Process word-level rules
        for ((regex, replacement) in wordRules) {
            result = regex.replace(result) { matchResult ->
                matchCase(matchResult.value, replacement)
            }
        }

        // 3. Ensure sentence start is capitalized
        return capitalizeSentences(result)
    }

    private fun matchCase(original: String, replacement: String): String {
        return when {
            original.length > 1 && original.all { it.isUpperCase() || !it.isLetter() } -> replacement.uppercase()
            else -> replacement.lowercase()
        }
    }

    private fun capitalizeSentences(text: String): String {
        val chars = text.toCharArray()
        var capitalizeNext = true
        for (i in chars.indices) {
            val c = chars[i]
            if (capitalizeNext && c.isLetter()) {
                chars[i] = c.uppercaseChar()
                capitalizeNext = false
            } else if (c == '.' || c == '!' || c == '?') {
                capitalizeNext = true
            }
        }
        return String(chars)
    }
}
