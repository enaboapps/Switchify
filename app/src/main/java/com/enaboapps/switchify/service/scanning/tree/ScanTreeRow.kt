package com.enaboapps.switchify.service.scanning.tree

import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * This class represents a row in the scan tree
 * @param nodes The nodes in the row
 * @param y The y coordinate of the row
 * @param individualHighlighting Whether the nodes should be highlighted individually
 */
class ScanTreeRow(
    val nodes: List<ScanNodeInterface>,
    val y: Int,
    val individualHighlighting: Boolean
) {
    private val window = SwitchifyAccessibilityWindow.instance
    private var boundsLayout: RelativeLayout? = null

    private val handler = Handler(Looper.getMainLooper())

    /**
     * This function highlights the row
     */
    fun highlight() {
        if (individualHighlighting || nodes.size == 1) {
            nodes.forEach { it.highlight() }
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
     * This function unhighlights the row
     */
    fun unhighlight() {
        if (individualHighlighting || nodes.size == 1) {
            nodes.forEach { it.unhighlight() }
        } else {
            boundsLayout?.let {
                handler.post {
                    window.removeView(it)
                }
            }
        }
    }

    /**
     * This function gets the x coordinate of the row
     * @return The x coordinate of the row
     */
    private fun getX(): Int {
        var minX = Int.MAX_VALUE
        nodes.forEach {
            if (it.getX() < minX) {
                minX = it.getX()
            }
        }
        return minX
    }

    /**
     * This function gets the width of the row
     * @return The width of the row
     */
    private fun getWidth(): Int {
        val firstX = nodes.minOfOrNull { it.getX() } ?: 0
        val lastX = nodes.maxOfOrNull { it.getX() + it.getWidth() } ?: 0
        return lastX - firstX
    }

    /**
     * This function gets the height of the row
     * @return The height of the row
     */
    private fun getHeight(): Int {
        val minY = nodes.minOfOrNull { it.getY() } ?: 0
        val maxY = nodes.maxOfOrNull { it.getY() + it.getHeight() } ?: 0
        return maxY - minY
    }
}