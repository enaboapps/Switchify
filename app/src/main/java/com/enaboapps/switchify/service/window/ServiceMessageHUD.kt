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
 * This class implements immediate message replacement, ensuring the most recent message is always displayed.
 *
 * @property context The application context, set via the setup method.
 * @property switchifyAccessibilityWindow The window manager for adding and removing views.
 * @property message The current message text being displayed.
 * @property shownMessageType The type of the current message being displayed.
 * @property messageView The LinearLayout containing the message TextView.
 * @property handler A Handler for posting delayed operations on the main thread.
 */
class ServiceMessageHUD private constructor() {
    companion object {
        /**
         * Singleton instance of ServiceMessageHUD.
         */
        val instance: ServiceMessageHUD by lazy {
            ServiceMessageHUD()
        }

        private const val TAG = "ServiceMessageHUD"
    }

    private var context: Context? = null
    private var switchifyAccessibilityWindow: SwitchifyAccessibilityWindow =
        SwitchifyAccessibilityWindow.instance
    private var message = ""
    private var shownMessageType: MessageType? = null
    private var messageView: LinearLayout? = null
    private val handler = Handler(Looper.getMainLooper())

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
         * A message that remains visible until explicitly cleared or replaced.
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
     * Creates or updates the message view with the specified message.
     * If the messageView doesn't exist, it creates a new one.
     * If it already exists, it updates the text of the existing view.
     */
    private fun createOrUpdateMessageView() {
        Log.d(TAG, "Creating or updating message view")
        context?.let { context ->
            if (messageView == null) {
                messageView = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    background = context.getDrawable(R.drawable.rounded_corners)
                    setPadding(16, 16, 16, 16)
                    layoutParams = LinearLayout.LayoutParams(
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
            } else {
                (messageView?.getChildAt(0) as? TextView)?.text = message
            }
            Log.d(TAG, "Message view created or updated successfully")
        } ?: Log.e(TAG, "Context is null, cannot create or update message view")
    }

    /**
     * Displays or updates a message on the screen, immediately replacing any existing message.
     *
     * @param message The message text to be displayed.
     * @param messageType The type of the message (DISAPPEARING or PERMANENT).
     * @param time The duration for which a DISAPPEARING message should be shown. Ignored for PERMANENT messages.
     */
    fun showMessage(message: String, messageType: MessageType, time: Time = Time.MEDIUM) {
        Log.d(TAG, "Showing message: $message, type: $messageType, time: $time")

        // Cancel any pending hide operations
        handler.removeCallbacksAndMessages(null)

        this.message = message
        this.shownMessageType = messageType

        createOrUpdateMessageView()

        if (messageView?.parent == null) {
            addViewToWindow()
        }

        if (messageType == MessageType.DISAPPEARING) {
            handler.postDelayed({ hideMessage() }, time.milliseconds)
            Log.d(TAG, "Scheduled message to disappear after ${time.milliseconds}ms")
        }
    }

    /**
     * Adds the message view to the window.
     * This method is called when a new message view is created and needs to be displayed.
     */
    private fun addViewToWindow() {
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
     * This method is called automatically for DISAPPEARING messages after their display time has elapsed,
     * or can be called manually to remove a message before its time has elapsed.
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
            }
        } ?: Log.d(TAG, "No message view to hide")
    }

    /**
     * Clears the current message, hiding it from the screen and resetting internal state.
     * This method can be called to explicitly remove any displayed message and reset the HUD state.
     */
    fun clearMessage() {
        Log.d(TAG, "Clearing message")
        hideMessage()
        message = ""
        shownMessageType = null
        messageView = null
    }
}