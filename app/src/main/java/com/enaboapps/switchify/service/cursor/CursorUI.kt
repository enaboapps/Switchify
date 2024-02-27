package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

class CursorUI(private val context: Context, private val handler: Handler) {
    private var xCursorLine: RelativeLayout? = null
    private var yCursorLine: RelativeLayout? = null
    private var xQuadrant: RelativeLayout? = null
    private var yQuadrant: RelativeLayout? = null

    private val window = SwitchifyAccessibilityWindow.instance

    // Constants for cursor and quadrant appearance
    companion object {
        private const val CURSOR_LINE_THICKNESS = 10
        private const val CURSOR_LINE_COLOR = Color.RED
        private const val QUADRANT_ALPHA = 0.5f
        private const val QUADRANT_COLOR = Color.BLUE
        private const val NUMBER_OF_QUADRANTS = 4
    }

    /**
     * Get the width of a quadrant
     */
    fun getQuadrantWidth(): Int {
        return CursorBounds.width(context) / NUMBER_OF_QUADRANTS
    }

    /**
     * Get the height of a quadrant
     */
    fun getQuadrantHeight(): Int {
        return CursorBounds.height(context) / NUMBER_OF_QUADRANTS
    }

    /**
     * Create a horizontal line at the given quadrant number
     */
    fun createXCursorLine(quadrantNumber: Int) {
        val xPosition = quadrantNumber * getQuadrantWidth()
        val yPosition = CursorBounds.yMin(context)
        val height = CursorBounds.height(context)
        xCursorLine = RelativeLayout(context).apply {
            setBackgroundColor(CURSOR_LINE_COLOR)
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
        val yPosition = CursorBounds.yMin(context) + (quadrantNumber * getQuadrantHeight())
        val width = CursorBounds.width(context)
        yCursorLine = RelativeLayout(context).apply {
            setBackgroundColor(CURSOR_LINE_COLOR)
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
        val yPosition = CursorBounds.yMin(context)
        val height = CursorBounds.height(context)
        val xPosition = quadrantNumber * getQuadrantWidth()
        xQuadrant = RelativeLayout(context).apply {
            setBackgroundColor(QUADRANT_COLOR)
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
        val yPosition = CursorBounds.yMin(context) + (quadrantNumber * getQuadrantHeight())
        yQuadrant = RelativeLayout(context).apply {
            setBackgroundColor(QUADRANT_COLOR)
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
                window.updateViewLayout(it, xPosition, CursorBounds.yMin(context))
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
                window.updateViewLayout(it, xPosition, CursorBounds.yMin(context))
            }
        }
    }

    /**
     * Update the vertical quadrant to the given quadrant number
     */
    fun updateYQuadrant(quadrantNumber: Int) {
        val quadrantHeight = getQuadrantHeight()
        val yPosition = CursorBounds.yMin(context) + (quadrantNumber * quadrantHeight)
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