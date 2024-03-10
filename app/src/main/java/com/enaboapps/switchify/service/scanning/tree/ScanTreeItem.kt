package com.enaboapps.switchify.service.scanning.tree

import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * This class represents an item in the scan tree
 * @property children The children of the item
 * @property y The y coordinate of the item
 * @property individualHighlighting If the children should be highlighted individually
 */
class ScanTreeItem(
    val children: List<ScanNodeInterface>,
    val y: Int,
    private val individualHighlighting: Boolean
) {
    private val window = SwitchifyAccessibilityWindow.instance
    private var boundsLayout: RelativeLayout? = null

    private val handler = Handler(Looper.getMainLooper())

    /**
     * This function highlights the item
     */
    fun highlight() {
        if (individualHighlighting || children.size == 1) {
            children.forEach { it.highlight() }
        } else {
            unhighlight()
            boundsLayout = RelativeLayout(window.getContext()).apply {
                background = window.getContext()
                    ?.getDrawable(com.enaboapps.switchify.R.drawable.scan_row_border)
            }
            boundsLayout?.let {
                handler.post {
                    window.addView(it, getX(), y, getWidth(), getHeight())
                }
            }
        }
    }

    /**
     * This function unhighlights the item
     */
    fun unhighlight() {
        if (individualHighlighting || children.size == 1) {
            children.forEach { it.unhighlight() }
        } else {
            boundsLayout?.let {
                handler.post {
                    window.removeView(it)
                }
            }
        }
    }

    /**
     * This function gets the x coordinate of the item
     * @return The x coordinate of the item
     */
    private fun getX(): Int {
        var minX = Int.MAX_VALUE
        children.forEach {
            if (it.getX() < minX) {
                minX = it.getX()
            }
        }
        return minX
    }

    /**
     * This function gets the width of the item
     * @return The width of the item
     */
    private fun getWidth(): Int {
        val firstX = children.minOfOrNull { it.getX() } ?: 0
        val lastX = children.maxOfOrNull { it.getX() + it.getWidth() } ?: 0
        return lastX - firstX
    }

    /**
     * This function gets the height of the item
     * @return The height of the item
     */
    private fun getHeight(): Int {
        val minY = children.minOfOrNull { it.getY() } ?: 0
        val maxY = children.maxOfOrNull { it.getY() + it.getHeight() } ?: 0
        return maxY - minY
    }
}