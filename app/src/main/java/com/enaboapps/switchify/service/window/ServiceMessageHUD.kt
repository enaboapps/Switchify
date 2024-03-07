package com.enaboapps.switchify.service.window

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.utils.ScreenUtils

/**
 * ServiceMessageHUD is responsible for displaying a message overlay on top of all other application windows.
 * It supports two types of messages: disappearing and permanent.
 */
class ServiceMessageHUD {
    private val TAG = "MessageManager"

    companion object {
        // Singleton instance to ensure only one instance of ServiceMessageHUD is used throughout the application.
        val instance: ServiceMessageHUD by lazy {
            ServiceMessageHUD()
        }
    }

    // The application context
    private var context: Context? = null

    // Reference to the accessibility service window manager
    private var switchifyAccessibilityWindow: SwitchifyAccessibilityWindow =
        SwitchifyAccessibilityWindow.instance

    // The message to be displayed
    private var message = ""

    // The type of the currently shown message
    private var shownMessageType: MessageType? = null

    // The LinearLayout that serves as the container for the message view
    private var messageView: LinearLayout? = null

    // Handler to post tasks on the main thread
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Sets up the message HUD with the necessary context.
     *
     * @param context The application context.
     */
    fun setup(context: Context) {
        this.context = context
    }

    /**
     * Enum to define the types of messages the HUD can show.
     */
    enum class MessageType {
        DISAPPEARING,
        PERMANENT
    }

    /**
     * Creates the message view with the specified configurations.
     */
    private fun createMessageView() {
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
        }
    }

    /**
     * Displays the message on the screen.
     *
     * @param message The message text to be displayed.
     * @param messageType The type of the message (DISAPPEARING or PERMANENT).
     */
    fun showMessage(message: String, messageType: MessageType) {
        if (message == this.message && messageType == this.shownMessageType) {
            return
        }

        if (shownMessageType == MessageType.DISAPPEARING) {
            return
        }

        this.message = message
        this.shownMessageType = messageType

        if (messageView != null) {
            hideMessage()
        }

        createMessageView()

        val x = 100
        val y = 150
        val width = ScreenUtils.getWidth(context!!) - 200

        handler.post {
            messageView?.let {
                switchifyAccessibilityWindow.addView(
                    it,
                    x,
                    y,
                    width,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
        }

        if (messageType == MessageType.DISAPPEARING) {
            handler.postDelayed({ hideMessage() }, 5000)
        }
    }

    /**
     * Hides and removes the currently displayed message from the screen.
     */
    private fun hideMessage() {
        messageView?.let { view ->
            handler.post {
                switchifyAccessibilityWindow.removeView(view)
                messageView = null
                shownMessageType = null
            }
        }
    }
}