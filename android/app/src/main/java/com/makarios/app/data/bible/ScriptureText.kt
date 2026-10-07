package com.makarios.app.data.bible

/**
 * Text pipeline cleaning utility for Bible verses.
 *
 * Cleans tokenization artifacts from raw translation sources:
 * - Removes erroneous whitespace before punctuation (. , ; : ? ! ))
 * - Removes whitespace after opening delimiters (( and opening quotes)
 * - Normalizes doubled closing quotes
 * - Collapses repeated whitespace
 */
object ScriptureText {

    fun clean(raw: String): String {
        if (raw.isBlank()) return ""
        var text = raw.trim()

        // Normalize doubled quotes e.g. ."" or ." " or ?" " or ""
        text = text.replace(Regex("\"\\s*\""), "\"")
        text = text.replace(Regex("”\\s*”"), "”")

        // Remove space before punctuation: . , ; : ? ! )
        text = text.replace(Regex("\\s+([.,;:?!\\)])"), "$1")

        // Remove space after opening parenthesis (
        text = text.replace(Regex("(\\()\\s+"), "$1")

        // Remove space after opening quotes: (" word, “ word)
        text = text.replace(Regex("(^|[\\s(\\[])[\"]\\s+"), "$1\"")
        text = text.replace(Regex("(^|[\\s(\\[])[“]\\s+"), "$1“")

        // Re-check space before punctuation that may have been adjacent to quotes
        text = text.replace(Regex("\\s+([.,;:?!\\)])"), "$1")

        // Normalize doubled closing quotes again after space removal
        text = text.replace(Regex("\"\\s*\""), "\"")
        text = text.replace(Regex("”\\s*”"), "”")

        // Collapse multiple whitespace down to single space
        text = text.replace(Regex("[ \\t]+"), " ")

        return text.trim()
    }

    /**
     * Formats selected verses for clipboard copy:
     * curly-quoted text, blank line, "Book Ch:vv WEB"
     */
    fun formatForCopy(reference: String, text: String, version: String = "WEB"): String {
        val cleaned = clean(text)
        // Format with curly quotes around the text, followed by blank line and citation
        return "“$cleaned”\n\n$reference $version"
    }
}
