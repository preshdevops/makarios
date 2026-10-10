package com.makarios.app.data.bible

/**
 * Parsed Scripture Reference representation.
 */
data class ParsedReference(
    val book: String,
    val chapter: Int,
    val fromVerse: Int? = null,
    val toVerse: Int? = null
) {
    fun formatDisplay(): String {
        return when {
            fromVerse != null && toVerse != null && fromVerse != toVerse -> "$book $chapter:$fromVerse-$toVerse"
            fromVerse != null -> "$book $chapter:$fromVerse"
            else -> "$book $chapter"
        }
    }
}

/**
 * Fast, robust Scripture Reference Parser handling book aliases,
 * chapters, verse ranges, spaces, colons, dots, and hyphens.
 */
object BibleReferenceParser {

    val CANONICAL_BOOKS = listOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy",
        "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel",
        "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs",
        "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations",
        "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk",
        "Zephaniah", "Haggai", "Zechariah", "Malachi", "Matthew",
        "Mark", "Luke", "John", "Acts", "Romans",
        "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians",
        "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy",
        "Titus", "Philemon", "Hebrews", "James", "1 Peter",
        "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
    )

    private val aliasMap: Map<String, String> = buildMap {
        fun register(target: String, vararg aliases: String) {
            put(target.lowercase(), target)
            put(target.lowercase().replace(" ", ""), target)
            aliases.forEach { a ->
                put(a.lowercase(), target)
                put(a.lowercase().replace(" ", ""), target)
                put(a.lowercase().replace(".", ""), target)
            }
        }

        register("Genesis", "gen", "ge", "gn")
        register("Exodus", "exo", "ex", "exod")
        register("Leviticus", "lev", "le", "lv")
        register("Numbers", "num", "nu", "nm", "nb")
        register("Deuteronomy", "deu", "deut", "dt")
        register("Joshua", "jos", "josh", "jsh")
        register("Judges", "jdg", "judg", "jg", "jdgs")
        register("Ruth", "rut", "rth", "ru")
        register("1 Samuel", "1sam", "1sa", "1s", "1 sam", "1 sa", "i sam", "i samuel", "first samuel")
        register("2 Samuel", "2sam", "2sa", "2s", "2 sam", "2 sa", "ii sam", "ii samuel", "second samuel")
        register("1 Kings", "1kgs", "1ki", "1k", "1 kgs", "1 ki", "1 king", "1 kings", "i kgs", "first kings")
        register("2 Kings", "2kgs", "2ki", "2k", "2 kgs", "2 ki", "2 king", "2 kings", "ii kgs", "second kings")
        register("1 Chronicles", "1chr", "1ch", "1 chr", "1 ch", "1 chron", "1 chronicles", "i chr", "first chronicles")
        register("2 Chronicles", "2chr", "2ch", "2 chr", "2 ch", "2 chron", "2 chronicles", "ii chr", "second chronicles")
        register("Ezra", "ezr", "ez")
        register("Nehemiah", "neh", "ne")
        register("Esther", "est", "esth", "es")
        register("Job", "jb")
        register("Psalms", "psalm", "psalms", "ps", "psa", "psm", "pss")
        register("Proverbs", "pro", "prov", "pr", "prv")
        register("Ecclesiastes", "ecc", "eccl", "ec", "qoh")
        register("Song of Solomon", "song", "songs", "sos", "song of songs", "canticle", "canticles", "cant")
        register("Isaiah", "isa", "is")
        register("Jeremiah", "jer", "je", "jr")
        register("Lamentations", "lam", "la")
        register("Ezekiel", "eze", "ezek", "ezk")
        register("Daniel", "dan", "da", "dn")
        register("Hosea", "hos", "ho")
        register("Joel", "joe", "jl")
        register("Amos", "amo", "am")
        register("Obadiah", "oba", "obad", "ob")
        register("Jonah", "jon", "jnh")
        register("Micah", "mic", "mc")
        register("Nahum", "nah", "na")
        register("Habakkuk", "hab", "hb")
        register("Zephaniah", "zep", "zeph", "zp")
        register("Haggai", "hag", "hg")
        register("Zechariah", "zec", "zech", "zc")
        register("Malachi", "mal", "ml")
        register("Matthew", "mat", "matt", "mt")
        register("Mark", "mrk", "mar", "mk", "mr")
        register("Luke", "luk", "lu", "lk")
        register("John", "jhn", "joh", "jn", "j")
        register("Acts", "act", "ac")
        register("Romans", "rom", "ro", "rm")
        register("1 Corinthians", "1cor", "1co", "1 cor", "1 co", "i cor", "i corinthians", "first corinthians")
        register("2 Corinthians", "2cor", "2co", "2 cor", "2 co", "ii cor", "ii corinthians", "second corinthians")
        register("Galatians", "gal", "ga")
        register("Ephesians", "eph", "ep")
        register("Philippians", "php", "phil", "phi", "pp")
        register("Colossians", "col", "co")
        register("1 Thessalonians", "1thess", "1th", "1 thess", "1 thessalonians", "1 th", "i thess", "first thessalonians")
        register("2 Thessalonians", "2thess", "2th", "2 thess", "2 thessalonians", "2 th", "ii thess", "second thessalonians")
        register("1 Timothy", "1tim", "1ti", "1 tim", "1 ti", "1 timothy", "i tim", "first timothy")
        register("2 Timothy", "2tim", "2ti", "2 tim", "2 ti", "2 timothy", "ii tim", "second timothy")
        register("Titus", "tit", "ti")
        register("Philemon", "phm", "philem", "pm")
        register("Hebrews", "heb", "he")
        register("James", "jas", "jm", "jam")
        register("1 Peter", "1pet", "1pe", "1pt", "1 pet", "1 pe", "1 pt", "1 peter", "i pet", "first peter")
        register("2 Peter", "2pet", "2pe", "2pt", "2 pet", "2 pe", "2 pt", "2 peter", "ii pet", "second peter")
        register("1 John", "1jhn", "1jn", "1j", "1 jhn", "1 jn", "1 john", "i john", "i jn", "first john")
        register("2 John", "2jhn", "2jn", "2j", "2 jhn", "2 jn", "2 john", "ii john", "ii jn", "second john")
        register("3 John", "3jhn", "3jn", "3j", "3 jhn", "3 jn", "3 john", "iii john", "iii jn", "third john")
        register("Jude", "jud", "jd")
        register("Revelation", "rev", "re", "rv", "apocalypse", "apoc")
    }

    /**
     * Resolves a raw book name or alias to canonical name, e.g. "Jn" -> "John", "Ps" -> "Psalms".
     */
    fun resolveBookName(raw: String): String? {
        val cleaned = raw.trim().lowercase().replace(".", "").replace(Regex("\\s+"), " ")
        aliasMap[cleaned]?.let { return it }
        val noSpaces = cleaned.replace(" ", "")
        aliasMap[noSpaces]?.let { return it }
        return null
    }

    /**
     * Parses a query string into a ParsedReference if valid, or null if it cannot be parsed as a reference.
     */
    fun parse(input: String): ParsedReference? {
        val trimmed = input.trim()
            .replace("â€“", "-")
            .replace("â€”", "-")
            .replace("–", "-")
            .replace("—", "-")
        if (trimmed.length < 2) return null

        // Regex patterns to capture:
        // Group 1: Book text (which may start with 1, 2, 3 or I, II, III)
        // Group 2: Chapter
        // Group 3: Optional verse start
        // Group 4: Optional verse end
        // Formats:
        // "John 3:16", "John 3:16-17", "John 3 16", "John 3.16", "1 cor 13:4-7", "Psalm 23"
        val patternWithVerse = Regex(
            "^([1-3Iivx]?\\s*[a-zA-Z\\s]+?)[\\s.:]+(\\d+)[\\s.:]+(\\d+)(?:\\s*-\\s*(\\d+))?$",
            RegexOption.IGNORE_CASE
        )
        val matchWithVerse = patternWithVerse.find(trimmed)
        if (matchWithVerse != null) {
            val rawBook = matchWithVerse.groupValues[1]
            val canonicalBook = resolveBookName(rawBook) ?: return null
            val chapter = matchWithVerse.groupValues[2].toIntOrNull() ?: return null
            val fromVerse = matchWithVerse.groupValues[3].toIntOrNull() ?: return null
            val toVerseRaw = matchWithVerse.groupValues[4]
            val toVerse = if (toVerseRaw.isNotBlank()) toVerseRaw.toIntOrNull() ?: fromVerse else fromVerse
            return ParsedReference(canonicalBook, chapter, fromVerse, toVerse)
        }

        // Pattern for Chapter-only e.g. "Romans 8", "Psalm 23", "1 Cor 13"
        val patternChapterOnly = Regex(
            "^([1-3Iivx]?\\s*[a-zA-Z\\s]+?)[\\s.:]+(\\d+)$",
            RegexOption.IGNORE_CASE
        )
        val matchChapterOnly = patternChapterOnly.find(trimmed)
        if (matchChapterOnly != null) {
            val rawBook = matchChapterOnly.groupValues[1]
            val canonicalBook = resolveBookName(rawBook) ?: return null
            val chapter = matchChapterOnly.groupValues[2].toIntOrNull() ?: return null
            return ParsedReference(canonicalBook, chapter, null, null)
        }

        // Just book name alone e.g. "Romans"
        val canonicalBookAlone = resolveBookName(trimmed)
        if (canonicalBookAlone != null) {
            return ParsedReference(canonicalBookAlone, 1, null, null)
        }

        return null
    }
}
