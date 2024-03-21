package com.enaboapps.switchify.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.enaboapps.switchify.R

class SwitchifyKeyboardService : InputMethodService(), KeyboardLayoutListener {

    private lateinit var keyboardLayout: LinearLayout

    override fun onCreateInputView(): View {
        // Create the main keyboard layout programmatically
        keyboardLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            background = resources.getDrawable(R.drawable.keyboard_background, null)
        }

        // Set the layout listener
        KeyboardLayoutManager.listener = this

        initializeKeyboardLayout(keyboardLayout)
        return keyboardLayout
    }

    private fun initializeKeyboardLayout(keyboardLayout: LinearLayout) {
        KeyboardLayoutManager.currentLayout.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val buttonLayoutParams =
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            // The third parameter (weight) ensures each button takes equal space within its row.

            row.forEach { keyType ->
                val keyButton = KeyboardKey(this).apply {
                    text = keyType.toString()
                    setOnClickListener { handleKeyPress(keyType) }
                    layoutParams = buttonLayoutParams
                }
                rowLayout.addView(keyButton)
            }

            keyboardLayout.addView(rowLayout)
        }
    }

    private fun handleKeyPress(keyType: KeyType) {
        when (keyType) {
            is KeyType.Character -> currentInputConnection.commitText(keyType.char, 1)
            KeyType.Backspace -> currentInputConnection.deleteSurroundingText(1, 0)
            KeyType.Space -> currentInputConnection.commitText(" ", 1)
            KeyType.Return -> currentInputConnection.commitText("\n", 1)
            KeyType.Shift -> KeyboardLayoutManager.toggleShift()
            KeyType.SwitchToSymbols -> KeyboardLayoutManager.switchLayout(KeyboardLayoutType.Symbols)
            KeyType.SwitchToAlphabetic -> KeyboardLayoutManager.switchLayout(KeyboardLayoutType.AlphabeticLower)
            else -> {} // Handle other key types as necessary
        }
    }

    override fun onLayoutChanged(layoutType: KeyboardLayoutType) {
        // Update the keyboard layout when the layout changes
        keyboardLayout.removeAllViews()
        initializeKeyboardLayout(keyboardLayout)
    }
}