package com.enaboapps.switchify.keyboard.utils

import android.view.inputmethod.EditorInfo

/**
 * Singleton object for handling capitalization modes in an Input Method Service.
 */
object CapsModeHandler {

    /**
     * Enum to define different capitalization modes.
     */
    enum class CapsMode {
        NONE,
        SENTENCES,
        WORDS,
        CHARACTERS
    }

    // Property to store the current capitalization mode
    var currentCapsMode: CapsMode = CapsMode.NONE
        private set

    /**
     * Determines and updates the current caps mode based on the EditorInfo provided by the currently focused field.
     * @param editorInfo The EditorInfo with details about the current input field.
     */
    fun updateCapsMode(editorInfo: EditorInfo) {
        currentCapsMode = when {
            editorInfo.inputType and EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES != 0 -> CapsMode.SENTENCES
            editorInfo.inputType and EditorInfo.TYPE_TEXT_FLAG_CAP_WORDS != 0 -> CapsMode.WORDS
            editorInfo.inputType and EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS != 0 -> CapsMode.CHARACTERS
            else -> CapsMode.NONE
        }
        println("Caps mode updated to: $currentCapsMode")
    }
}