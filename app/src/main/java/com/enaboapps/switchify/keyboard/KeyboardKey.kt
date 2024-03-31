package com.enaboapps.switchify.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.enaboapps.switchify.R

class KeyboardKey @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var action: (() -> Unit)? = null

    private var button: Button? = null
    private var imageButton: ImageButton? = null

    init {
        orientation = VERTICAL // Or HORIZONTAL, depending on your design
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
            setOnClickListener { action?.invoke() }
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                getHeightInDp()
            )
            setTextColor(Color.WHITE)
            background =
                ResourcesCompat.getDrawable(resources, R.drawable.keyboard_key_background, null)
        }
        addView(button)
    }

    private fun addImageView(drawable: Drawable) {
        imageButton = ImageButton(context).apply {
            setImageDrawable(drawable)
            setOnClickListener { action?.invoke() }
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