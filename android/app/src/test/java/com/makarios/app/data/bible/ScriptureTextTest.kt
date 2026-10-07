package com.makarios.app.data.bible

import org.junit.Assert.assertEquals
import org.junit.Test

class ScriptureTextTest {

    @Test
    fun testMatthew3_14() {
        val raw = "But John would have hindered him , saying , \" I need to be baptized by you , and you come to me ?\""
        val expected = "But John would have hindered him, saying, \"I need to be baptized by you, and you come to me?\""
        assertEquals(expected, ScriptureText.clean(raw))
    }

    @Test
    fun testMatthew3_15() {
        val raw = "But Jesus , answering, said to him , \"Allow it now , for this is the fitting way for us to fulfill all righteousness .\" Then he allowed him ."
        val expected = "But Jesus, answering, said to him, \"Allow it now, for this is the fitting way for us to fulfill all righteousness.\" Then he allowed him."
        assertEquals(expected, ScriptureText.clean(raw))
    }

    @Test
    fun testMatthew3_16() {
        val raw = "Jesus , when he was baptized, went up directly from the water : and behold , the heavens were opened to him . He saw the Spirit of God descending as a dove , and coming on him ."
        val expected = "Jesus, when he was baptized, went up directly from the water: and behold, the heavens were opened to him. He saw the Spirit of God descending as a dove, and coming on him."
        assertEquals(expected, ScriptureText.clean(raw))
    }

    @Test
    fun testMatthew3_17() {
        val raw = "Behold , a voice out of the heavens said , \" This is my beloved Son , with whom I am well pleased .\""
        val expected = "Behold, a voice out of the heavens said, \"This is my beloved Son, with whom I am well pleased.\""
        assertEquals(expected, ScriptureText.clean(raw))
    }

    @Test
    fun testDoubledQuotesNormalized() {
        val raw = "And he said , \" It is done .\" \""
        val expected = "And he said, \"It is done.\""
        assertEquals(expected, ScriptureText.clean(raw))
    }

    @Test
    fun testParenthesesSpacing() {
        val raw = "In the beginning , God ( Elohim ) created the heavens ."
        val expected = "In the beginning, God (Elohim) created the heavens."
        assertEquals(expected, ScriptureText.clean(raw))
    }

    @Test
    fun testIdempotence() {
        val raw = "Behold , a voice out of the heavens said , \" This is my beloved Son , with whom I am well pleased .\""
        val once = ScriptureText.clean(raw)
        val twice = ScriptureText.clean(once)
        assertEquals(once, twice)
    }

    @Test
    fun testFormatForCopy() {
        val ref = "Matthew 3:17"
        val text = "Behold, a voice out of the heavens said, \"This is my beloved Son, with whom I am well pleased.\""
        val expected = "“Behold, a voice out of the heavens said, \"This is my beloved Son, with whom I am well pleased.\"”\n\nMatthew 3:17 WEB"
        assertEquals(expected, ScriptureText.formatForCopy(ref, text, "WEB"))
    }
}
