package com.enaboapps.switchify.service.menu

import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.AUTO_SIZE_TEXT_TYPE_NONE
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.properties.Delegates

/**
 * This class represents a menu item
 * @property id The id of the menu item
 * @property text The text of the menu item
 * @property drawableId The drawable resource id of the menu item
 * @property drawableDescription The description of the drawable
 * @property closeOnSelect Whether the menu should close when the item is selected
 * @property isLinkToMenu Whether the item is a link to another menu
 * @property isMenuHierarchyManipulator Whether the item manipulates the menu hierarchy
 * @property page The page of the menu item
 * @property action The action to perform when the item is selected
 * @property visible Whether the menu item is visible
 */
class MenuItem(
    val id: String,
    private val text: String = "",
    private val drawableId: Int = 0,
    private val drawableDescription: String = "",
    val closeOnSelect: Boolean = true,
    var isLinkToMenu: Boolean = false,
    var isMenuHierarchyManipulator: Boolean = false,
    private var page: Int = 0,
    private val action: () -> Unit,
    var visible: Boolean = true
) {
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

    // Navy color for the foreground
    private var foregroundColor by Delegates.notNull<Int>()

    /**
     * Inflate the menu item
     * @param linearLayout The linear layout to inflate the menu item into
     * @param width The width of the menu item
     * @param height The height of the menu item
     */
    fun inflate(linearLayout: LinearLayout, width: Int = 90, height: Int = 75) {
        if (!visible) return

        val widthPx = ScreenUtils.dpToPx(linearLayout.context, width)
        val heightPx = ScreenUtils.dpToPx(linearLayout.context, height)

        view = LinearLayout(linearLayout.context).apply {
            layoutParams = LinearLayout.LayoutParams(widthPx, heightPx).apply {
                weight = 1f
            }
            minimumWidth = widthPx
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

        foregroundColor = linearLayout.context.resources.getColor(R.color.navy, null)

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
                    foregroundColor
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
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                setAutoSizeTextTypeWithDefaults(AUTO_SIZE_TEXT_TYPE_NONE)
                gravity = Gravity.CENTER
                setTextColor(foregroundColor)
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
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
                setAutoSizeTextTypeWithDefaults(AUTO_SIZE_TEXT_TYPE_NONE)
                setTextColor(foregroundColor)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
                setPadding(padding, 0, padding, padding)
                view?.addView(this)
            }
        }

        linearLayout.addView(view)
    }

    /**
     * Select the menu item
     */
    fun select() {
        if (!isLinkToMenu && !isMenuHierarchyManipulator && closeOnSelect) {
            MenuManager.getInstance().closeMenuHierarchy()
        }
        action()
    }

    /**
     * Get the background drawable
     * @return The background drawable
     */
    private fun getBackgroundDrawable(): Int {
        return R.drawable.service_key_background
    }

    /**
     * Get the location of the menu item on the screen
     * @return The location of the menu item on the screen
     */
    private fun getLocationOnScreen(): IntArray {
        val location = IntArray(2)
        view?.getLocationOnScreen(location)
        return location
    }

    /**
     * Get the x coordinate of the menu item
     * @return The x coordinate of the menu item
     */
    val x: Int
        get() = getLocationOnScreen()[0]

    /**
     * Get the y coordinate of the menu item
     * @return The y coordinate of the menu item
     */
    val y: Int
        get() = getLocationOnScreen()[1]

    /**
     * Get the width of the menu item
     * @return The width of the menu item
     */
    val width: Int
        get() = view?.width ?: 0

    /**
     * Get the height of the menu item
     * @return The height of the menu item
     */
    val height: Int
        get() = view?.height ?: 0
}
