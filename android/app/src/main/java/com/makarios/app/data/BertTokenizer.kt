package com.makarios.app.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * On-device BERT / WordPiece tokenizer for all-MiniLM models.
 * Reads vocab.txt from app assets and tokenizes user input strings
 * into token IDs, attention masks, and token type IDs.
 */
class BertTokenizer private constructor(private val vocab: Map<String, Long>) {

    companion object {
        const val CLS_TOKEN = "[CLS]"
        const val SEP_TOKEN = "[SEP]"
        const val UNK_TOKEN = "[UNK]"
        const val PAD_TOKEN = "[PAD]"

        private const val CLS_ID = 101L
        private const val SEP_ID = 102L
        private const val UNK_ID = 100L
        private const val PAD_ID = 0L

        private var instance: BertTokenizer? = null

        fun getInstance(context: Context): BertTokenizer {
            return instance ?: synchronized(this) {
                instance ?: loadFromAssets(context).also { instance = it }
            }
        }

        private fun loadFromAssets(context: Context): BertTokenizer {
            val vocabMap = mutableMapOf<String, Long>()
            context.assets.open("vocab.txt").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).useLines { lines ->
                    var idx = 0L
                    lines.forEach { line ->
                        val token = line.trim()
                        if (token.isNotEmpty()) {
                            vocabMap[token] = idx
                        }
                        idx++
                    }
                }
            }
            return BertTokenizer(vocabMap)
        }
    }

    data class TokenizedInput(
        val inputIds: LongArray,
        val attentionMask: LongArray,
        val tokenTypeIds: LongArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as TokenizedInput
            return inputIds.contentEquals(other.inputIds) &&
                    attentionMask.contentEquals(other.attentionMask) &&
                    tokenTypeIds.contentEquals(other.tokenTypeIds)
        }

        override fun hashCode(): Int {
            var result = inputIds.contentHashCode()
            result = 31 * result + attentionMask.contentHashCode()
            result = 31 * result + tokenTypeIds.contentHashCode()
            return result
        }
    }

    /**
     * Tokenizes a text sentence into BERT model inputs.
     * Prepend [CLS], append [SEP], maximum sequence length clamped.
     */
    fun tokenize(text: String, maxSeqLength: Int = 128): TokenizedInput {
        val tokens = mutableListOf<Long>()
        tokens.add(vocab[CLS_TOKEN] ?: CLS_ID)

        val words = basicTokenize(text)
        for (word in words) {
            val subwords = wordPieceTokenize(word)
            for (subword in subwords) {
                if (tokens.size >= maxSeqLength - 1) break
                tokens.add(subword)
            }
            if (tokens.size >= maxSeqLength - 1) break
        }

        tokens.add(vocab[SEP_TOKEN] ?: SEP_ID)

        val inputIds = tokens.toLongArray()
        val attentionMask = LongArray(inputIds.size) { 1L }
        val tokenTypeIds = LongArray(inputIds.size) { 0L }

        return TokenizedInput(inputIds, attentionMask, tokenTypeIds)
    }

    /**
     * Splits text into lowercase alphanumeric and punctuation tokens.
     */
    private fun basicTokenize(text: String): List<String> {
        val clean = text.lowercase().trim()
        val tokens = mutableListOf<String>()
        val currentWord = StringBuilder()

        for (ch in clean) {
            if (ch.isWhitespace()) {
                if (currentWord.isNotEmpty()) {
                    tokens.add(currentWord.toString())
                    currentWord.clear()
                }
            } else if (isPunctuation(ch)) {
                if (currentWord.isNotEmpty()) {
                    tokens.add(currentWord.toString())
                    currentWord.clear()
                }
                tokens.add(ch.toString())
            } else {
                currentWord.append(ch)
            }
        }
        if (currentWord.isNotEmpty()) {
            tokens.add(currentWord.toString())
        }
        return tokens
    }

    private fun isPunctuation(ch: Char): Boolean {
        val type = Character.getType(ch).toByte()
        return type == Character.CONNECTOR_PUNCTUATION ||
                type == Character.DASH_PUNCTUATION ||
                type == Character.START_PUNCTUATION ||
                type == Character.END_PUNCTUATION ||
                type == Character.INITIAL_QUOTE_PUNCTUATION ||
                type == Character.FINAL_QUOTE_PUNCTUATION ||
                type == Character.OTHER_PUNCTUATION
    }

    /**
     * Splits a word into WordPiece subword token IDs using greedy longest-match-first.
     */
    private fun wordPieceTokenize(word: String): List<Long> {
        val subwords = mutableListOf<Long>()
        var start = 0
        val unkId = vocab[UNK_TOKEN] ?: UNK_ID

        while (start < word.length) {
            var end = word.length
            var matchedId: Long? = null

            while (start < end) {
                val substr = word.substring(start, end)
                val piece = if (start == 0) substr else "##$substr"
                if (vocab.containsKey(piece)) {
                    matchedId = vocab[piece]
                    break
                }
                end--
            }

            if (matchedId == null) {
                return listOf(unkId)
            }

            subwords.add(matchedId)
            start = end
        }

        return subwords
    }
}
