package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanColorManager
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

class CursorUI(private val context: Context, private val handler: Handler) {
    private var xCursorLine: RelativeLayout? = null
    private var yCursorLine: RelativeLayout? = null
    private var xQuadrant: RelativeLayout? = null
    private var yQuadrant: RelativeLayout? = null

    private val window = SwitchifyAccessibilityWindow.instance

    // Constants for cursor and quadrant appearance
    companion object {
        const val CURSOR_LINE_THICKNESS = 10
        private const val QUADRANT_ALPHA = 0.5f

        /**
         * This function determines the number of quadrants
         * It uses the size of the cursor bounds to determine the number of quadrants
         * @param size The size of the cursor bounds
         * @return The number of quadrants
         */
        fun getNumberOfQuadrants(size: Int): Int {
            val minThreshold = 500
            val quarterBounds = size / 4
            return if (CursorMode.isSingleMode()) {
                1
            } else if (quarterBounds < minThreshold) {
                2
            } else {
                4
            }
        }
    }

    /**
     * Get the width of a quadrant
     */
    fun getQuadrantWidth(): Int {
        return if (CursorMode.isBlockMode()) {
            CursorBounds.width(context) / getNumberOfQuadrants(CursorBounds.width(context))
        } else {
            CursorBounds.width(context)
        }
    }

    /**
     * Get the height of a quadrant
     */
    fun getQuadrantHeight(): Int {
        return if (CursorMode.isBlockMode()) {
            CursorBounds.height(context) / getNumberOfQuadrants(CursorBounds.height(context))
        } else {
            CursorBounds.height(context)
        }
    }

    /**
     * Create a horizontal line at the given quadrant number
     */
    fun createXCursorLine(quadrantNumber: Int) {
        val xPosition = quadrantNumber * getQuadrantWidth()
        val yPosition = CursorBounds.Y_MIN
        val height = CursorBounds.height(context)
        xCursorLine = RelativeLayout(context).apply {
            val color = ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor
            setBackgroundColor(Color.parseColor(color))
        }
        handler.post {
            xCursorLine?.let {
                window.addView(it, xPosition, yPosition, CURSOR_LINE_THICKNESS, height)
            }
        }
    }

    /**
     * Create a vertical line at the given quadrant number
     */
    fun createYCursorLine(quadrantNumber: Int) {
        val xPosition = CursorBounds.X_MIN
        val yPosition = CursorBounds.Y_MIN + (quadrantNumber * getQuadrantHeight())
        val width = CursorBounds.width(context)
        yCursorLine = RelativeLayout(context).apply {
            val color = ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor
            setBackgroundColor(Color.parseColor(color))
        }
        handler.post {
            yCursorLine?.let {
                window.addView(it, xPosition, yPosition, width, CURSOR_LINE_THICKNESS)
            }
        }
    }

    /**
     * Create a horizontal quadrant at the given quadrant number
     */
    fun createXQuadrant(quadrantNumber: Int) {
        val yPosition = CursorBounds.Y_MIN
        val height = CursorBounds.height(context)
        val xPosition = quadrantNumber * getQuadrantWidth()
        xQuadrant = RelativeLayout(context).apply {
            setBackgroundColor(
                Color.parseColor(
                    ScanColorManager.getScanColorSetFromPreferences(
                        context
                    ).primaryColor
                )
            )
            alpha = QUADRANT_ALPHA
        }
        handler.post {
            xQuadrant?.let {
                window.addView(it, xPosition, yPosition, getQuadrantWidth(), height)
            }
        }
    }

    /**
     * Create a vertical quadrant at the given quadrant number
     */
    fun createYQuadrant(quadrantNumber: Int) {
        val xPosition = CursorBounds.X_MIN
        val width = CursorBounds.width(context)
        val yPosition = CursorBounds.Y_MIN + (quadrantNumber * getQuadrantHeight())
        yQuadrant = RelativeLayout(context).apply {
            setBackgroundColor(
                Color.parseColor(
                    ScanColorManager.getScanColorSetFromPreferences(
                        context
                    ).primaryColor
                )
            )
            alpha = QUADRANT_ALPHA
        }
        handler.post {
            yQuadrant?.let {
                window.addView(it, xPosition, yPosition, width, getQuadrantHeight())
            }
        }
    }

    /**
     * Remove the horizontal line
     */
    fun removeXCursorLine() {
        xCursorLine?.let {
            handler.post {
                window.removeView(it)
            }
            xCursorLine = null
        }
    }

    /**
     * Remove the vertical line
     */
    fun removeYCursorLine() {
        yCursorLine?.let {
            handler.post {
                window.removeView(it)
            }
            yCursorLine = null
        }
    }

    /**
     * Remove the horizontal quadrant
     */
    fun removeXQuadrant() {
        xQuadrant?.let {
            handler.post {
                window.removeView(it)
            }
            xQuadrant = null
        }
    }

    /**
     * Remove the vertical quadrant
     */
    fun removeYQuadrant() {
        yQuadrant?.let {
            handler.post {
                window.removeView(it)
            }
            yQuadrant = null
        }
    }

    /**
     * Update the horizontal line to the given x position
     */
    fun updateXCursorLine(xPosition: Int) {
        xCursorLine?.let {
            handler.post {
                window.updateViewLayout(it, xPosition, CursorBounds.Y_MIN)
            }
        }
    }

    /**
     * Update the vertical line to the given y position
     */
    fun updateYCursorLine(yPosition: Int) {
        yCursorLine?.let {
            handler.post {
                window.updateViewLayout(it, 0, yPosition)
            }
        }
    }

    /**
     * Update the horizontal quadrant to the given quadrant number
     */
    fun updateXQuadrant(quadrantNumber: Int) {
        val quadrantWidth = getQuadrantWidth()
        val xPosition = quadrantNumber * quadrantWidth
        xQuadrant?.let {
            handler.post {
                window.updateViewLayout(it, xPosition, CursorBounds.Y_MIN)
            }
        }
    }

    /**
     * Update the vertical quadrant to the given quadrant number
     */
    fun updateYQuadrant(quadrantNumber: Int) {
        val quadrantHeight = getQuadrantHeight()
        val yPosition = CursorBounds.Y_MIN + (quadrantNumber * quadrantHeight)
        yQuadrant?.let {
            handler.post {
                window.updateViewLayout(it, 0, yPosition)
            }
        }
    }

    /**
     * Check if the cursor UI is reset
     */
    fun isReset(): Boolean {
        return xCursorLine == null && yCursorLine == null && xQuadrant == null && yQuadrant == null
    }

    /**
     * Reset the cursor UI
     */
    fun reset() {
        removeXCursorLine()
        removeYCursorLine()
        removeXQuadrant()
        removeYQuadrant()
    }

    /**
     * Check if the horizontal line is visible
     */
    fun isXCursorLineVisible(): Boolean {
        return xCursorLine != null
    }

    /**
     * Check if the vertical line is visible
     */
    fun isYCursorLineVisible(): Boolean {
        return yCursorLine != null
    }

    /**
     * Check if the horizontal quadrant is visible
     */
    fun isXQuadrantVisible(): Boolean {
        return xQuadrant != null
    }

    /**
     * Check if the vertical quadrant is visible
     */
    fun isYQuadrantVisible(): Boolean {
        return yQuadrant != null
    }
}