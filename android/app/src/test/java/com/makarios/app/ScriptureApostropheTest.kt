package com.makarios.app

import com.makarios.app.data.bible.ScriptureText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ScriptureApostropheTest {
    @Test fun firstJohnFourTwentyHasNoStrayApostropheSpace() {
        // 1 John 4:20 sentence fragment as it arrives with the tokenisation bug
        val cleaned = ScriptureText.clean("If a man say, I love God, and hateth his brother, he is a liar: for he that loveth not his brother whom he hath seen, how can he love God whom he hath not seen? And this commandment have we from him, That he who loveth God love his brother also. It doesn' t change.")
        assertFalse(cleaned.contains("doesn' t"))
        assertEquals(true, cleaned.contains("doesn't"))
    }
    @Test fun curlyApostropheNormalised() {
        assertEquals("don't", ScriptureText.clean("don’t"))
        assertEquals("doesn't", ScriptureText.clean("doesn’ t"))
    }
}
