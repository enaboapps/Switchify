package com.enaboapps.switchify.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import com.enaboapps.switchify.R

class KeyboardKey @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var action: (() -> Unit)? = null

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
        val button = Button(context).apply {
            setText(text)
            setOnClickListener { action?.invoke() }
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                225
            )
            setTextColor(Color.WHITE)
            background = context.getDrawable(R.drawable.keyboard_key_background)
        }
        addView(button)
    }

    private fun addImageView(drawable: Drawable) {
        val imageButton = ImageButton(context).apply {
            setImageDrawable(drawable)
            setOnClickListener { action?.invoke() }
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                225
            )
            background = context.getDrawable(R.drawable.keyboard_key_background)
        }
        addView(imageButton)
    }
}