package com.enaboapps.switchify.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView.AUTO_SIZE_TEXT_TYPE_NONE
import androidx.core.content.res.ResourcesCompat
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.utils.ScreenUtils

class KeyboardKey @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var tapAction: (() -> Unit)? = null
    var holdAction: (() -> Unit)? = null

    private var button: Button? = null
    private var imageButton: ImageButton? = null

    private val handler = Handler(Looper.getMainLooper())
    private var isHolding = false

    private val repeatUpdateTask = object : Runnable {
        override fun run() {
            if (isHolding) {
                holdAction?.invoke()
                handler.postDelayed(this, 100) // subsequent repeats every 100ms
            }
        }
    }

    private val onTouchListener = OnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isHolding = false // Reset the holding flag
                handler.postDelayed({
                    if (isHolding) {
                        holdAction?.invoke() // Trigger initial hold action after delay
                        handler.postDelayed(repeatUpdateTask, 100) // Start repeating
                    }
                }, 400) // Wait for long press threshold
                isHolding = true
                return@OnTouchListener true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isHolding) {
                    handler.removeCallbacksAndMessages(null)
                    isHolding = false
                    if (event.action == MotionEvent.ACTION_UP) {
                        tapAction?.invoke() // Execute tap action if it was a tap
                        view.performClick() // Ensure performClick is called for accessibility
                    }
                }
                return@OnTouchListener true
            }
        }
        false
    }

    init {
        orientation = VERTICAL // Or HORIZONTAL, depending on your design
        val padding = ScreenUtils.dpToPx(context, 2)
        setPadding(padding, padding, padding, padding)
    }

    fun setKeyContent(text: String? = null, drawable: Drawable? = null) {
        removeAllViews() // Clear previous content
        when {
            text != null -> addTextView(text)
            drawable != null -> addImageView(drawable)
            else -> throw IllegalArgumentException("Either text or drawable must be provided")
        }
    }

    private fun addTextView(text: String) {
        button = Button(context).apply {
            setText(text)
            setOnTouchListener(onTouchListener)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                getHeightInDp()
            )
            isAllCaps = false
            setTextColor(Color.BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            setAutoSizeTextTypeWithDefaults(AUTO_SIZE_TEXT_TYPE_NONE)
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.keyboard_key_background, null)
        }
        addView(button)
    }

    private fun addImageView(drawable: Drawable) {
        imageButton = ImageButton(context).apply {
            setImageDrawable(drawable)
            setColorFilter(Color.BLACK)
            setOnTouchListener(onTouchListener)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                getHeightInDp()
            )
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.keyboard_key_background, null)
        }
        addView(imageButton)
    }

    private fun getHeightInDp(): Int {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val percentage = 0.08 // 8% of the screen height
        return (screenHeight * percentage).toInt()
    }

    fun setPinned(pinned: Boolean) {
        val background = if (pinned) {
            ResourcesCompat.getDrawable(resources, R.drawable.keyboard_key_background_pinned, null)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.keyboard_key_background, null)
        }
        button?.background = background
        imageButton?.background = background
    }
}