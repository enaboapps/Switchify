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

class ServiceMessageHUD {
    private val TAG = "MessageManager"

    companion object {
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

    fun setup(context: Context) {
        this.context = context
    }

    enum class MessageType {
        DISAPPEARING,
        PERMANENT
    }


    private fun createMessageView() {
        context?.let {
            messageView = LinearLayout(it)
            messageView?.orientation = LinearLayout.VERTICAL
            messageView?.gravity = Gravity.CENTER
            messageView?.background = it.getDrawable(R.drawable.rounded_corners)
            messageView?.setPadding(16, 16, 16, 16)
            messageView?.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val messageTextView = TextView(it)
            messageTextView.text = message
            messageTextView.setTextColor(Color.WHITE)
            messageTextView.textSize = 20f

            messageView?.addView(messageTextView)
        }
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
        val y = ScreenUtils.getHeight(context!!) - 300

        handler.post {
            if (messageView != null) {
                switchifyAccessibilityWindow.addView(
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


    private fun hideMessage() {
        if (messageView != null) {
            handler.post {
                switchifyAccessibilityWindow.removeView(messageView!!)
                messageView = null
                shownMessageType = null
            }
        }
    }
}