package com.makarios.app

import com.makarios.app.data.bible.BibleReferenceParser
import com.makarios.app.data.bible.ParsedReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BibleReferenceParserTest {

    @Test
    fun testFortyDiverseInputs() {
        val testCases = listOf(
            // 1-10: Standard with colons, dots, spaces
            "John 3:16" to ParsedReference("John", 3, 16, 16),
            "john 3 16" to ParsedReference("John", 3, 16, 16),
            "JOHN 3:16" to ParsedReference("John", 3, 16, 16),
            "Jn 3:16" to ParsedReference("John", 3, 16, 16),
            "jn 3:16" to ParsedReference("John", 3, 16, 16),
            "jhn 3:16" to ParsedReference("John", 3, 16, 16),
            "John 3.16" to ParsedReference("John", 3, 16, 16),
            "1 cor 13:4-7" to ParsedReference("1 Corinthians", 13, 4, 7),
            "1Cor 13:4-7" to ParsedReference("1 Corinthians", 13, 4, 7),
            "1cor 13:4" to ParsedReference("1 Corinthians", 13, 4, 4),

            // 11-20: Numbered books and ranges
            "2 Cor 5:17" to ParsedReference("2 Corinthians", 5, 17, 17),
            "2cor 5:17" to ParsedReference("2 Corinthians", 5, 17, 17),
            "1 Peter 5:7" to ParsedReference("1 Peter", 5, 7, 7),
            "1pet 5:7" to ParsedReference("1 Peter", 5, 7, 7),
            "2 Pet 1:3" to ParsedReference("2 Peter", 1, 3, 3),
            "1 John 4:18" to ParsedReference("1 John", 4, 18, 18),
            "1jn 4:18" to ParsedReference("1 John", 4, 18, 18),
            "1 Sam 16:7" to ParsedReference("1 Samuel", 16, 7, 7),
            "2 Samuel 22:31" to ParsedReference("2 Samuel", 22, 31, 31),
            "1 Kings 19:12" to ParsedReference("1 Kings", 19, 12, 12),

            // 21-30: Wisdom literature, prophets & short names
            "Psalm 23" to ParsedReference("Psalms", 23, null, null),
            "Psalms 23" to ParsedReference("Psalms", 23, null, null),
            "Ps 23:1-3" to ParsedReference("Psalms", 23, 1, 3),
            "psalm 23:1" to ParsedReference("Psalms", 23, 1, 1),
            "Proverbs 3:5" to ParsedReference("Proverbs", 3, 5, 5),
            "prov 3:5-6" to ParsedReference("Proverbs", 3, 5, 6),
            "Song of Solomon 2:1" to ParsedReference("Song of Solomon", 2, 1, 1),
            "Song 2:1" to ParsedReference("Song of Solomon", 2, 1, 1),
            "Songs 2:1" to ParsedReference("Song of Solomon", 2, 1, 1),
            "sos 2:1" to ParsedReference("Song of Solomon", 2, 1, 1),

            // 31-40: Gospels, Paul, General Epistles & Revelation
            "Rom 8:28" to ParsedReference("Romans", 8, 28, 28),
            "romans 8" to ParsedReference("Romans", 8, null, null),
            "Matt 6:33" to ParsedReference("Matthew", 6, 33, 33),
            "mt 6:33" to ParsedReference("Matthew", 6, 33, 33),
            "Luke 1:37" to ParsedReference("Luke", 1, 37, 37),
            "lk 1:37" to ParsedReference("Luke", 1, 37, 37),
            "Phil 4:13" to ParsedReference("Philippians", 4, 13, 13),
            "php 4:19" to ParsedReference("Philippians", 4, 19, 19),
            "Rev 21:4" to ParsedReference("Revelation", 21, 4, 4),
            "apocalypse 21:4" to ParsedReference("Revelation", 21, 4, 4),

            // 41-45: Extra validations
            "Gen 1:1" to ParsedReference("Genesis", 1, 1, 1),
            "Genesis 1:1-3" to ParsedReference("Genesis", 1, 1, 3),
            "Isa 40:31" to ParsedReference("Isaiah", 40, 31, 31),
            "is 40:31" to ParsedReference("Isaiah", 40, 31, 31),
            "Jer 29:11" to ParsedReference("Jeremiah", 29, 11, 11)
        )

        for ((input, expected) in testCases) {
            val parsed = BibleReferenceParser.parse(input)
            assertNotNull("Failed to parse reference: $input", parsed)
            assertEquals("Mismatch for input '$input'", expected, parsed)
        }
    }

    @Test
    fun testInvalidInputsReturnNull() {
        assertNull(BibleReferenceParser.parse("fear"))
        assertNull(BibleReferenceParser.parse("peace and joy"))
        assertNull(BibleReferenceParser.parse("not a verse"))
        assertNull(BibleReferenceParser.parse(""))
        assertNull(BibleReferenceParser.parse("x"))
    }
}
