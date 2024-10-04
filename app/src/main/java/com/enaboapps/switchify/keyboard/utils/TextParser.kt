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

    fun getLatestParagraph(): String {
        // Find the last newline character
        val index = allText.lastIndexOf("\n")

        val length = if (index == -1) allText.length else index + 1

        if (index == -1) return allText

        // Return the substring from the last newline character to the end of the text
        return allText.substring(length)
    }

    fun getLengthOfWhitespacesAtEndOfLatestSentence(): Int {
        var index = allText.length - 1
        var count = 0
        while (index >= 0 && allText[index] == ' ') {
            if (isNewline(allText[index])) {
                break
            }
            count++
            index--
        }
        return count
    }

    fun getLengthOfWordToDelete(): Int {
        var index = allText.length - 1
        // first we need to get the count of whitespaces at the end of the latest sentence
        var count = getLengthOfWhitespacesAtEndOfLatestSentence()
        index -= count
        // next we need to delete the count of characters until we reach a whitespace
        while (index >= 0 && allText[index] != ' ') {
            count++
            index--
        }
        return count
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

    fun latestWordHasNumber(): Boolean {
        return wordsInLatestSentence.isNotEmpty() && wordsInLatestSentence[wordsInLatestSentence.size - 1].matches(
            ".*\\d.*".toRegex()
        )
    }

    fun isNewSentence(): Boolean = newSentence

    fun isNewWord(): Boolean {
        if (allText.isEmpty()) return true
        return allText[allText.length - 1] == ' '
    }

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

    fun shouldFormatSpecialCharacter(char: Char): Boolean {
        return char.toString().matches("[.,!?]".toRegex())
    }

    private fun isSentenceDelimiter(ch: Char): Boolean = ch.toString().matches("[.!?]".toRegex())

    private fun isAlphanumeric(ch: Char): Boolean = ch.toString().matches("\\p{Alnum}".toRegex())

    private fun isNewline(ch: Char): Boolean = ch.toString().matches("\\n".toRegex())
}