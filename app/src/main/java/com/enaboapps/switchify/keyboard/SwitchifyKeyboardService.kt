package com.enaboapps.switchify.keyboard

import android.content.Intent
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.R
import com.enaboapps.switchify.keyboard.prediction.PredictionListener
import com.enaboapps.switchify.keyboard.prediction.PredictionManager
import com.enaboapps.switchify.keyboard.prediction.PredictionView
import com.enaboapps.switchify.keyboard.utils.TextParser

/**
 * This class is responsible for managing the keyboard service.
 * It extends InputMethodService and implements KeyboardLayoutListener.
 */
class SwitchifyKeyboardService : InputMethodService(), KeyboardLayoutListener, PredictionListener {

    // The main keyboard layout
    private lateinit var keyboardLayout: LinearLayout

    // The keyboard accessibility manager
    private lateinit var keyboardAccessibilityManager: KeyboardAccessibilityManager

    // The global layout listener
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    // The prediction manager
    private var predictionManager: PredictionManager? = null

    // The prediction view
    private lateinit var predictionView: PredictionView

    // The text parser
    private val textParser = TextParser.getInstance()

    companion object {
        const val ACTION_KEYBOARD_SHOW = "com.enaboapps.switchify.keyboard.ACTION_KEYBOARD_SHOW"
        const val ACTION_KEYBOARD_HIDE = "com.enaboapps.switchify.keyboard.ACTION_KEYBOARD_HIDE"
    }

    /**
     * This method is called when the service is created.
     * It initializes the keyboard accessibility manager and the prediction manager.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize the keyboard accessibility manager
        keyboardAccessibilityManager = KeyboardAccessibilityManager(this)

        // Set the layout listener
        KeyboardLayoutManager.listener = this

        // Initialize the prediction manager
        predictionManager = PredictionManager(this, this)
        predictionManager?.initialize()
    }

    /**
     * This method is called when the input view is created.
     * It initializes the keyboard layout and the keyboard accessibility manager.
     */
    override fun onCreateInputView(): View {
        // Create the main keyboard layout programmatically
        keyboardLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.keyboard_background, null)
        }

        // Initialize the prediction view
        predictionView = PredictionView(this) { prediction ->
            handleKeyPress(prediction)
        }

        // Initialize the keyboard layout
        initializeKeyboardLayout(keyboardLayout)

        return keyboardLayout
    }

    /**
     * This method is called when the input view is started.
     * It updates the keyboard layout and adds the global layout listener.
     */
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Add the global layout listener when the input view is started
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            keyboardAccessibilityManager.captureAndBroadcastLayoutInfo(keyboardLayout)
        }
        keyboardLayout.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

        updateTextState()

        // Broadcast keyboard show event
        val intent = Intent(ACTION_KEYBOARD_SHOW)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * This method is called when the input view is finished.
     * It removes the global layout listener.
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        // Remove the global layout listener when the input view is finished
        keyboardLayout.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)

        // Broadcast keyboard hide event
        val intent = Intent(ACTION_KEYBOARD_HIDE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * This method is called when the text selection changes.
     */
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )

        updateTextState()
    }

    /**
     * This method is called when the text changes.
     */
    private fun updateTextState() {
        currentInputConnection?.let {
            val text = it.getTextBeforeCursor(100, 0).toString()
            textParser.parseText(text)
            predictionManager?.predict(text)
            updateShiftState()
        }
    }

    /**
     * This method is called when the predictions are available.
     */
    override fun onPredictionsAvailable(predictions: List<String>) {
        predictionView.setPredictions(predictions)
        predictionView.updateCase()
        println("Predictions available: $predictions")
    }

    /**
     * This method initializes the keyboard layout.
     * It creates a new row layout for each row in the current layout,
     * and a new key button for each key type in the row.
     */
    private fun initializeKeyboardLayout(keyboardLayout: LinearLayout) {
        // Set up the predictions view
        keyboardLayout.addView(predictionView)

        KeyboardLayoutManager.currentLayout.forEach { row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.forEach { type ->
                val keyButton = KeyboardKey(this).apply {
                    // Set a higher weight for the space key
                    val weight = if (type is KeyType.Space) 3f else 1f
                    layoutParams =
                        LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
                    action = { handleKeyPress(type) }
                    if (getDrawableResource(type) != null) {
                        setKeyContent(drawable = getDrawableResource(type))
                    } else {
                        setKeyContent(text = type.toString())
                    }
                    if (type is KeyType.ShiftCaps) {
                        setPinned(KeyboardLayoutManager.currentLayoutState != KeyboardLayoutState.Lower)
                    }
                }
                rowLayout.addView(keyButton)
            }

            keyboardLayout.addView(rowLayout)
        }
    }

    /**
     * This function returns the correct drawable resource for the given key type.
     *
     * @param keyType the key type.
     * @return the drawable resource.
     */
    private fun getDrawableResource(keyType: KeyType): Drawable? {
        if (keyType is KeyType.Backspace) {
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_backspace, null)
        }
        if (keyType is KeyType.Return) {
            return ResourcesCompat.getDrawable(resources, R.drawable.ic_return, null)
        }
        if (keyType is KeyType.ShiftCaps) {
            return if (KeyboardLayoutManager.currentLayoutState == KeyboardLayoutState.Lower ||
                KeyboardLayoutManager.currentLayoutState == KeyboardLayoutState.Shift
            ) {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_shift, null)
            } else {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_caps, null)
            }
        }
        return null
    }

    /**
     * This method updates the shift state of the keyboard based on the current text.
     */
    private fun updateShiftState() {
        val isNewSentence = textParser.isNewSentence()
        if (KeyboardLayoutManager.currentLayoutState == KeyboardLayoutState.Lower && isNewSentence) {
            KeyboardLayoutManager.toggleState()
        }
    }

    /**
     * This method handles key press events.
     * It performs different actions based on the type of the key.
     */
    private fun handleKeyPress(keyType: KeyType) {
        when (keyType) {
            is KeyType.Character -> {
                val text = keyType.char
                currentInputConnection.commitText(text, 1)
                KeyboardLayoutManager.updateStateAfterInput()
            }

            is KeyType.Special -> {
                val text = keyType.symbol
                if (textParser.shouldFormatSpecialCharacter(text[0])) {
                    val whitespace = textParser.getLengthOfWhitespacesAtEndOfLatestSentence()
                    currentInputConnection.deleteSurroundingText(whitespace, 0)
                    currentInputConnection.commitText(text, 1)
                    currentInputConnection.commitText(" ", 1)
                } else {
                    currentInputConnection.commitText(text, 1)
                }
                KeyboardLayoutManager.updateStateAfterInput()
            }

            is KeyType.Prediction -> {
                val text = keyType.prediction
                val currentWordLength =
                    textParser.getWordFromLatestSentenceBySubtractingNumberFromLastIndex(0).length
                currentInputConnection.deleteSurroundingText(currentWordLength, 0)
                currentInputConnection.commitText(text, 1)
                currentInputConnection.commitText(" ", 1)
                KeyboardLayoutManager.updateStateAfterInput()
            }

            KeyType.ShiftCaps -> {
                KeyboardLayoutManager.toggleState()
                predictionView.updateCase()
            }

            KeyType.SwitchToAlphabetic -> {
                if (KeyboardLayoutManager.currentLayoutState == KeyboardLayoutState.Shift ||
                    KeyboardLayoutManager.currentLayoutState == KeyboardLayoutState.Caps
                ) {
                    KeyboardLayoutManager.switchLayout(KeyboardLayoutType.AlphabeticUpper)
                } else {
                    KeyboardLayoutManager.switchLayout(KeyboardLayoutType.AlphabeticLower)
                }
            }

            KeyType.Space -> {
                currentInputConnection.commitText(" ", 1)
                KeyboardLayoutManager.updateStateAfterInput()
            }

            KeyType.Return -> {
                currentInputConnection.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_ENTER
                    )
                )
                currentInputConnection.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_ENTER
                    )
                )
            }

            KeyType.Backspace -> {
                currentInputConnection.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_DEL
                    )
                )
                currentInputConnection.sendKeyEvent(
                    android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_DEL
                    )
                )
            }

            KeyType.SwitchToSymbols -> {
                KeyboardLayoutManager.switchLayout(KeyboardLayoutType.Symbols)
            }
        }
    }

    /**
     * This method is called when the keyboard layout changes.
     * It updates the keyboard layout.
     */
    override fun onLayoutChanged(layoutType: KeyboardLayoutType) {
        // Update the keyboard layout when the layout changes
        keyboardLayout.removeAllViews()
        initializeKeyboardLayout(keyboardLayout)
    }
}