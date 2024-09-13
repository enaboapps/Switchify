package com.enaboapps.switchify.service.window

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.utils.ScreenUtils

/**
 * ServiceMessageHUD is responsible for displaying overlay messages on top of all other application windows.
 * It supports two types of messages: disappearing and permanent, with configurable display durations.
 * This class implements a debounce mechanism to prevent rapid successive message displays.
 *
 * This class follows the Singleton pattern to ensure only one instance manages all overlay messages.
 */
class ServiceMessageHUD private constructor() {
    private val TAG = "ServiceMessageHUD"

    companion object {
        /**
         * Singleton instance of ServiceMessageHUD.
         */
        val instance: ServiceMessageHUD by lazy {
            ServiceMessageHUD()
        }
    }

    private var context: Context? = null
    private var switchifyAccessibilityWindow: SwitchifyAccessibilityWindow =
        SwitchifyAccessibilityWindow.instance
    private var message = ""
    private var shownMessageType: MessageType? = null
    private var messageView: LinearLayout? = null
    private val handler = Handler(Looper.getMainLooper())

    // Debounce mechanism properties
    private var lastShowTime: Long = 0
    private val debounceInterval: Long = 250 // 250ms debounce interval

    /**
     * Sets up the ServiceMessageHUD with the necessary context.
     *
     * @param context The application context.
     */
    fun setup(context: Context) {
        this.context = context
        Log.d(TAG, "ServiceMessageHUD setup with context: $context")
    }

    /**
     * Defines the types of messages that can be displayed.
     */
    enum class MessageType {
        /**
         * A message that automatically disappears after a specified duration.
         */
        DISAPPEARING,

        /**
         * A message that remains visible until explicitly cleared.
         */
        PERMANENT
    }

    /**
     * Defines preset durations for disappearing messages.
     *
     * @property milliseconds The duration in milliseconds.
     */
    enum class Time(val milliseconds: Long) {
        /**
         * A short duration of 1 second.
         */
        SHORT(1000),

        /**
         * A medium duration of 5 seconds.
         */
        MEDIUM(5000),

        /**
         * A long duration of 10 seconds.
         */
        LONG(10000)
    }

    /**
     * Creates the message view with the specified configurations.
     */
    private fun createMessageView() {
        Log.d(TAG, "Creating message view")
        context?.let { context ->
            messageView = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = context.getDrawable(R.drawable.rounded_corners)
                setPadding(16, 16, 16, 16)
                layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
            }

            val messageTextView = TextView(context).apply {
                text = message
                setTextColor(Color.WHITE)
                textSize = 20f
                gravity = Gravity.CENTER
            }

            messageView?.addView(messageTextView)
            Log.d(TAG, "Message view created successfully")
        } ?: Log.e(TAG, "Context is null, cannot create message view")
    }

    /**
     * Displays or updates a message on the screen.
     * Implements a debounce mechanism to prevent showing multiple messages within a 250ms window.
     *
     * @param message The message text to be displayed.
     * @param messageType The type of the message (DISAPPEARING or PERMANENT).
     * @param time The duration for which a DISAPPEARING message should be shown. Ignored for PERMANENT messages.
     */
    fun showMessage(message: String, messageType: MessageType, time: Time = Time.MEDIUM) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShowTime < debounceInterval) {
            Log.d(TAG, "Skipping message due to debounce: $message")
            return
        }

        lastShowTime = currentTime
        Log.d(TAG, "Showing message: $message, type: $messageType, time: $time")

        if (message == this.message && messageType == this.shownMessageType) {
            Log.d(TAG, "Message and type unchanged, returning")
            return
        }

        this.message = message
        this.shownMessageType = messageType

        if (messageView != null) {
            updateMessageView()
        } else {
            createAndShowMessageView()
        }

        if (messageType == MessageType.DISAPPEARING) {
            handler.postDelayed({ hideMessage() }, time.milliseconds)
            Log.d(TAG, "Scheduled message to disappear after ${time.milliseconds}ms")
        }
    }

    /**
     * Updates the text of an existing message view.
     */
    private fun updateMessageView() {
        Log.d(TAG, "Updating message view")
        handler.post {
            (messageView?.getChildAt(0) as? TextView)?.text = message
            Log.d(TAG, "Message view updated successfully")
        }
    }

    /**
     * Creates a new message view and displays it on the screen.
     */
    private fun createAndShowMessageView() {
        Log.d(TAG, "Creating and showing message view")
        createMessageView()

        val x = 100
        val y = 150
        val width = ScreenUtils.getWidth(context!!) - 200

        handler.post {
            messageView?.let {
                try {
                    switchifyAccessibilityWindow.addView(
                        it,
                        x,
                        y,
                        width,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    Log.d(TAG, "Message view added to window successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add view to window", e)
                }
            } ?: Log.e(TAG, "MessageView is null, cannot add to window")
        }
    }

    /**
     * Hides and removes the currently displayed message from the screen.
     */
    private fun hideMessage() {
        Log.d(TAG, "Hiding message")
        messageView?.let { view ->
            handler.post {
                try {
                    switchifyAccessibilityWindow.removeView(view)
                    Log.d(TAG, "Message view removed from window successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to remove view from window", e)
                }
                messageView = null
                shownMessageType = null
            }
        } ?: Log.d(TAG, "No message view to hide")
    }

    /**
     * Clears the current message, hiding it from the screen and resetting internal state.
     */
    fun clearMessage() {
        Log.d(TAG, "Clearing message")
        hideMessage()
        message = ""
        shownMessageType = null
    }
}