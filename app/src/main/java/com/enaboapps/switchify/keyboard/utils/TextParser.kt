package com.enaboapps.switchify.keyboard.utils

import android.util.Log

class TextParser private constructor() {
    private var allText: String = ""
    private var sentences: ArrayList<String> = arrayListOf()
    private var wordsInLatestSentence: ArrayList<String> = arrayListOf()
    private var newSentence: Boolean = false

    companion object {
        private const val TAG = "TextParser"
        private var instance: TextParser? = null

        fun getInstance(): TextParser {
            if (instance == null) {
                instance = TextParser()
            }
            return instance!!
        }
    }

    fun getAllText(): String = allText

    fun getLengthOfTextAfterLatestWord(): Int {
        var index = allText.length - 1
        var length = 0
        while (index > 0) {
            if (isAlphanumeric(allText[index])) return length
            length++
            index--
        }
        return 0
    }

    fun getWordFromLatestSentenceBySubtractingNumberFromLastIndex(number: Int): String {
        return if (wordsInLatestSentence.size > number && !newSentence) {
            wordsInLatestSentence[wordsInLatestSentence.size - number - 1]
        } else ""
    }

    fun getSentenceBySubtractingNumberFromLastIndex(number: Int): String {
        return if (sentences.size > number && !newSentence) {
            sentences[sentences.size - number - 1]
        } else ""
    }

    fun isNewSentence(): Boolean = newSentence

    fun parseText(text: String) {
        Log.d(TAG, "parseText with $text")

        allText = text
        sentences = arrayListOf()
        text.split("(?<=[.!?])\\s*".toRegex()).forEach {
            sentences.add(it)
        }

        var index: Int
        wordsInLatestSentence = arrayListOf()
        var latestSentence = sentences.last()
        index = text.length - 1
        if (index > 0) {
            while (!isAlphanumeric(text[index]) && index > 0) {
                try {
                    latestSentence = latestSentence.substring(0, index)
                    index--
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
        latestSentence.split("\\s".toRegex()).forEach {
            wordsInLatestSentence.add(it)
        }
        val isLastWordEmpty = (index != text.length - 1)
        if (isLastWordEmpty) wordsInLatestSentence.add("")

        index = text.length - 1
        newSentence = (text == "") // if true, the following while loop will not run
        if (index > 0) {
            while (!isAlphanumeric(text[index]) && index > 0) {
                if (isSentenceDelimiter(text[index]) || isNewline(text[index])) {
                    newSentence = true
                    break
                }
                index--
            }
        }
    }

    private fun isSentenceDelimiter(ch: Char): Boolean = ch.toString().matches("[.!?]".toRegex())

    private fun isAlphanumeric(ch: Char): Boolean = ch.toString().matches("\\p{Alnum}".toRegex())

    private fun isNewline(ch: Char): Boolean = ch.toString().matches("\\n".toRegex())
}