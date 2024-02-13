package com.enaboapps.switchify.service.menu

import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat

/**
 * This class represents a menu item
 * @property text The text of the menu item
 * @property drawableId The drawable resource id of the menu item
 * @property closeOnSelect Whether the menu should close when the item is selected
 * @property isLinkToMenu Whether the item is a link to another menu
 * @property isMenuHierarchyManipulator Whether the item manipulates the menu hierarchy
 * @property page The page of the menu item
 * @property action The action to perform when the item is selected
 */
class MenuItem(
    private val text: String = "",
    private val drawableId: Int = 0,
    val closeOnSelect: Boolean = true,
    var isLinkToMenu: Boolean = false,
    var isMenuHierarchyManipulator: Boolean = false,
    var page: Int = 0,
    private val action: () -> Unit
) {
    /**
     * The highlighted state of the menu item
     */
    private var highlighted = false

    /**
     * The view of the menu item
     */
    private var view: LinearLayout? = null

    /**
     * The image view of the menu item
     */
    private var imageView: ImageView? = null

    /**
     * The text view of the menu item
     */
    private var textView: TextView? = null

    /**
     * Inflate the menu item
     * @param linearLayout The linear layout to inflate the menu item into
     * @param wrapHorizontal Whether to wrap the menu item horizontally
     */
    fun inflate(linearLayout: LinearLayout, wrapHorizontal: Boolean = false) {
        highlighted = false
        view = LinearLayout(linearLayout.context)
        view?.layoutParams = LinearLayout.LayoutParams(
            if (wrapHorizontal) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        view?.gravity = Gravity.CENTER

        val padding = 20

        if (drawableId != 0) {
            imageView = ImageView(linearLayout.context)
            // get the drawable in white
            val wrappedDrawable = DrawableCompat.wrap(
                linearLayout.context.resources.getDrawable(drawableId, null)
            ).mutate()
            DrawableCompat.setTint(
                wrappedDrawable,
                linearLayout.context.resources.getColor(getForegroundColor(), null)
            )
            imageView?.setImageDrawable(wrappedDrawable)
            imageView?.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            imageView?.setPadding(padding, padding, padding, padding)
            view?.addView(imageView)
        }

        if (text.isNotEmpty()) {
            textView = TextView(linearLayout.context)
            textView?.text = text
            textView?.setTextColor(
                linearLayout.context.resources.getColor(
                    getForegroundColor(),
                    null
                )
            )
            textView?.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView?.setPadding(padding, padding, padding, padding)
            view?.addView(textView)
        }

        view?.setBackgroundColor(
            linearLayout.context.resources.getColor(
                getBackgroundColor(),
                null
            )
        )
        view?.setOnClickListener {
            select()
        }
        linearLayout.addView(view)
    }

    /**
     * Get the correct background color for the menu item
     * @return The background color
     */
    private fun getBackgroundColor(): Int {
        return if (highlighted) {
            android.R.color.white
        } else {
            android.R.color.black
        }
    }

    /**
     * Get the correct foreground color for the menu item
     * @return The foreground color
     */
    private fun getForegroundColor(): Int {
        return if (highlighted) {
            android.R.color.black
        } else {
            android.R.color.white
        }
    }

    /**
     * Select the menu item
     */
    fun select() {
        Log.d("MenuItem", "Selected menu item: $text")
        if (!isLinkToMenu && !isMenuHierarchyManipulator && closeOnSelect) {
            MenuManager.getInstance().closeMenuHierarchy()
        }
        action()
    }

    /**
     * Highlight the menu item
     */
    fun highlight() {
        try {
            highlighted = true
            view?.setBackgroundColor(
                view?.context?.resources?.getColor(
                    getBackgroundColor(),
                    null
                )!!
            )
            textView?.setTextColor(
                textView?.context?.resources?.getColor(
                    getForegroundColor(),
                    null
                )!!
            )
            imageView?.setColorFilter(
                imageView?.context?.resources?.getColor(
                    getForegroundColor(),
                    null
                )!!
            )
        } catch (e: Exception) {
            Log.e("MenuItem", "Error highlighting menu item", e)
        }
    }

    /**
     * Unhighlight the menu item
     */
    fun unhighlight() {
        try {
            highlighted = false
            view?.setBackgroundColor(
                view?.context?.resources?.getColor(
                    getBackgroundColor(),
                    null
                )!!
            )
            textView?.setTextColor(
                textView?.context?.resources?.getColor(
                    getForegroundColor(),
                    null
                )!!
            )
            imageView?.setColorFilter(
                imageView?.context?.resources?.getColor(
                    getForegroundColor(),
                    null
                )!!
            )
        } catch (e: Exception) {
            Log.e("MenuItem", "Error unhighlighting menu item", e)
        }
    }
}