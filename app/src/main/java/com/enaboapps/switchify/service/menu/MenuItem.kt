package com.enaboapps.switchify.service.menu

import android.graphics.text.LineBreaker
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.utils.ScreenUtils

/**
 * This class represents a menu item
 * @property text The text of the menu item
 * @property drawableId The drawable resource id of the menu item
 * @property drawableDescription The description of the drawable
 * @property closeOnSelect Whether the menu should close when the item is selected
 * @property isLinkToMenu Whether the item is a link to another menu
 * @property isMenuHierarchyManipulator Whether the item manipulates the menu hierarchy
 * @property page The page of the menu item
 * @property action The action to perform when the item is selected
 */
class MenuItem(
    private val text: String = "",
    private val drawableId: Int = 0,
    private val drawableDescription: String = "",
    val closeOnSelect: Boolean = true,
    var isLinkToMenu: Boolean = false,
    var isMenuHierarchyManipulator: Boolean = false,
    var page: Int = 0,
    private val action: () -> Unit
) : ScanNodeInterface {
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
     * The text view for the drawable description
     */
    private var drawableDescriptionTextView: TextView? = null

    /**
     * The text view of the menu item
     */
    private var textView: TextView? = null

    /**
     * Inflate the menu item
     * @param linearLayout The linear layout to inflate the menu item into
     */
    fun inflate(linearLayout: LinearLayout) {
        val width = ScreenUtils.dpToPx(linearLayout.context, 85)
        val height = ScreenUtils.dpToPx(linearLayout.context, 75)

        highlighted = false

        view = LinearLayout(linearLayout.context).apply {
            layoutParams = LinearLayout.LayoutParams(width, height)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = ResourcesCompat.getDrawable(
                linearLayout.context.resources,
                getBackgroundDrawable(),
                null
            )
            setOnClickListener { select() }
        }

        val padding = 20

        if (drawableId != 0) {
            imageView = ImageView(linearLayout.context).apply {
                val wrappedDrawable = DrawableCompat.wrap(
                    ResourcesCompat.getDrawable(
                        linearLayout.context.resources,
                        drawableId,
                        null
                    )!!
                ).mutate()
                DrawableCompat.setTint(
                    wrappedDrawable,
                    linearLayout.context.resources.getColor(getForegroundColor(), null)
                )
                setImageDrawable(wrappedDrawable)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(padding, padding, padding, padding)
                view?.addView(this)
            }
        }

        if (text.isNotEmpty()) {
            textView = TextView(linearLayout.context).apply {
                text = this@MenuItem.text
                textSize = 14f
                justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                gravity = Gravity.CENTER
                setAutoSizeTextTypeUniformWithConfiguration(10, 20, 1, 0)
                setTextColor(linearLayout.context.resources.getColor(getForegroundColor(), null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(padding, padding, padding, padding)
                view?.addView(this)
            }
        }

        if (drawableDescription.isNotEmpty()) {
            drawableDescriptionTextView = TextView(linearLayout.context).apply {
                text = drawableDescription
                textSize = 10f
                setTextColor(linearLayout.context.resources.getColor(getForegroundColor(), null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(padding, 0, padding, padding)
                visibility = View.GONE // Initially hidden
                view?.addView(this)
            }
        }

        linearLayout.addView(view)
    }

    /**
     * Get the correct background drawable for the menu item
     * @return The background drawable
     */
    private fun getBackgroundDrawable(): Int {
        return if (highlighted) {
            R.drawable.menu_item_background_highlighted
        } else {
            R.drawable.menu_item_background
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
    override fun select() {
        Log.d("MenuItem", "Selected menu item: $text")
        if (!isLinkToMenu && !isMenuHierarchyManipulator && closeOnSelect) {
            MenuManager.getInstance().closeMenuHierarchy()
        }
        action()
    }

    /**
     * Highlight the menu item
     */
    override fun highlight() {
        view?.post {
            try {
                highlighted = true
                view?.background = ResourcesCompat.getDrawable(
                    view?.context?.resources!!,
                    getBackgroundDrawable(),
                    null
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
                drawableDescriptionTextView?.setTextColor(
                    drawableDescriptionTextView?.context?.resources?.getColor(
                        getForegroundColor(),
                        null
                    )!!
                )
                drawableDescriptionTextView?.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("MenuItem", "Error highlighting menu item", e)
            }
        }
    }

    /**
     * Unhighlight the menu item
     */
    override fun unhighlight() {
        view?.post {
            try {
                highlighted = false
                view?.background = ResourcesCompat.getDrawable(
                    view?.context?.resources!!,
                    getBackgroundDrawable(),
                    null
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
                drawableDescriptionTextView?.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("MenuItem", "Error unhighlighting menu item", e)
            }
        }
    }

    override fun getMidX(): Int {
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        return location[0] + (view?.width ?: 0) / 2
    }

    override fun getMidY(): Int {
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        return location[1] + (view?.height ?: 0) / 2
    }

    override fun getLeft(): Int {
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        return location[0]
    }

    override fun getTop(): Int {
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        return location[1]
    }

    override fun getWidth(): Int {
        return view?.width ?: 0
    }

    override fun getHeight(): Int {
        return view?.height ?: 0
    }
}