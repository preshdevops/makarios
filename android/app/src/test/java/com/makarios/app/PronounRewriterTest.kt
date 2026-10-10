package com.makarios.app

import com.makarios.app.util.PronounRewriter
import org.junit.Assert.assertEquals
import org.junit.Test

class PronounRewriterTest {

    @Test
    fun testIAmToYouAre() {
        val input = "I am strong and courageous."
        val output = PronounRewriter.rewrite(input)
        assertEquals("You are strong and courageous.", output)
    }

    @Test
    fun testImToYoure() {
        val input = "I'm not forgotten."
        val output = PronounRewriter.rewrite(input)
        assertEquals("You're not forgotten.", output)
    }

    @Test
    fun testMyToYour() {
        val input = "My heart is fixed on God."
        val output = PronounRewriter.rewrite(input)
        assertEquals("Your heart is fixed on God.", output)
    }

    @Test
    fun testMeToYou() {
        val input = "The Lord is watching over me."
        val output = PronounRewriter.rewrite(input)
        assertEquals("The Lord is watching over you.", output)
    }

    @Test
    fun testIWasToYouWere() {
        val input = "I was made for such a time as this."
        val output = PronounRewriter.rewrite(input)
        assertEquals("You were made for such a time as this.", output)
    }

    @Test
    fun testComplexMultiPronounSentence() {
        val input = "I have peace because my God fights for me and I will not fear."
        val output = PronounRewriter.rewrite(input)
        assertEquals("You have peace because your God fights for you and you will not fear.", output)
    }

    @Test
    fun testMyselfToYourself() {
        val input = "I speak blessing over myself."
        val output = PronounRewriter.rewrite(input)
        assertEquals("You speak blessing over yourself.", output)
    }
}
