package com.enaboapps.switchify.service.window

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.enaboapps.switchify.service.utils.ScreenUtils
import java.util.Timer

class ServiceMessageHUD {
    private val TAG = "MessageManager"

    companion object {
        val instance: ServiceMessageHUD by lazy {
            ServiceMessageHUD()
        }
    }

    private var switchifyAccessibilityWindow: SwitchifyAccessibilityWindow? = null

    private var message = ""
    private var shownMessageType: MessageType? = null

    private var messageView: LinearLayout? = null

    private val handler = Handler(Looper.getMainLooper())

    enum class MessageType {
        DISAPPEARING,
        PERMANENT
    }


    fun setup(switchifyAccessibilityWindow: SwitchifyAccessibilityWindow) {
        this.switchifyAccessibilityWindow = switchifyAccessibilityWindow
    }


    private fun createMessageView() {
        messageView = LinearLayout(switchifyAccessibilityWindow?.getContext())
        messageView?.orientation = LinearLayout.VERTICAL
        messageView?.gravity = Gravity.CENTER
        messageView?.setBackgroundColor(Color.parseColor("#CC000000"))
        messageView?.setPadding(16, 16, 16, 16)
        messageView?.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val messageTextView = TextView(switchifyAccessibilityWindow?.getContext())
        messageTextView.text = message
        messageTextView.setTextColor(Color.WHITE)
        messageTextView.textSize = 20f

        messageView?.addView(messageTextView)
    }


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

        // Bottom of screen
        val y = ScreenUtils.getHeight(switchifyAccessibilityWindow?.getContext()!!) - 300

        handler.post {
            if (messageView != null) {
                switchifyAccessibilityWindow?.addView(
                    messageView!!,
                    0,
                    y,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
        }

        if (messageType == MessageType.DISAPPEARING) {
            // Remove the message after 5 seconds
            handler.postDelayed({
                hideMessage()
            }, 5000)
        }
    }


    fun hideMessage() {
        if (messageView != null) {
            handler.post {
                switchifyAccessibilityWindow?.removeView(messageView!!)
                messageView = null
                shownMessageType = null
            }
        }
    }
}