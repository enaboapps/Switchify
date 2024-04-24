package com.enaboapps.switchify.keyboard.utils

import android.view.inputmethod.EditorInfo
import com.enaboapps.switchify.keyboard.KeyboardLayoutManager
import com.enaboapps.switchify.keyboard.KeyboardLayoutState
import java.util.Locale

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

    /**
     * Gets the passed string with the appropriate capitalization based on the current caps mode.
     * @param text The text to capitalize.
     */
    fun getCapitalizedText(text: String): String {
        return when (KeyboardLayoutManager.currentLayoutState) {
            KeyboardLayoutState.Lower -> text
            KeyboardLayoutState.Shift -> text.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }

            KeyboardLayoutState.Caps -> text.uppercase(Locale.ROOT)
        }
    }
}