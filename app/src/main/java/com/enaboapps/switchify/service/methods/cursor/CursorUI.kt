package com.enaboapps.switchify.service.methods.cursor

import android.content.Context
import android.graphics.Color
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.methods.shared.ScanMethodUIConstants
import com.enaboapps.switchify.service.scanning.ScanColorManager
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * CursorUI class handles the creation, updating, and removal of cursor lines and quadrants
 * for the Switchify accessibility service.
 *
 * @property context The application context.
 */
class CursorUI(private val context: Context) {
    private var xCursorLine: RelativeLayout? = null
    private var yCursorLine: RelativeLayout? = null
    private var xQuadrant: RelativeLayout? = null
    private var yQuadrant: RelativeLayout? = null

    private val window = SwitchifyAccessibilityWindow.instance

    companion object {
        private const val QUADRANT_ALPHA = 0.5f

        /**
         * Determines the number of quadrants based on the cursor mode.
         *
         * @return The number of quadrants (1 for single mode, 4 for block mode).
         */
        fun getNumberOfQuadrants(): Int {
            return if (CursorMode.isSingleMode()) 1 else 4
        }
    }

    /**
     * Calculates the width of a quadrant based on the cursor mode.
     *
     * @return The width of a quadrant in pixels.
     */
    fun getQuadrantWidth(): Int {
        return if (CursorMode.isBlockMode()) {
            CursorBounds.width(context) / getNumberOfQuadrants()
        } else {
            CursorBounds.width(context)
        }
    }

    /**
     * Calculates the height of a quadrant based on the cursor mode.
     *
     * @return The height of a quadrant in pixels.
     */
    fun getQuadrantHeight(): Int {
        return if (CursorMode.isBlockMode()) {
            CursorBounds.height(context) / getNumberOfQuadrants()
        } else {
            CursorBounds.height(context)
        }
    }

    /**
     * Shows or updates the horizontal cursor line.
     *
     * @param x The x-coordinate for the line.
     */
    fun showXCursorLine(x: Int) {
        val yPosition = CursorBounds.Y_MIN
        val height = CursorBounds.height(context)

        if (xCursorLine == null) {
            xCursorLine = RelativeLayout(context).apply {
                val color = ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor
                setBackgroundColor(Color.parseColor(color))
            }
            xCursorLine?.let {
                window.addView(
                    it,
                    x,
                    yPosition,
                    ScanMethodUIConstants.LINE_THICKNESS,
                    height
                )
            }
        } else {
            updateXCursorLine(x)
        }
    }

    /**
     * Shows or updates the vertical cursor line.
     *
     * @param y The y-coordinate for the line.
     */
    fun showYCursorLine(y: Int) {
        val xPosition = CursorBounds.X_MIN
        val width = CursorBounds.width(context)

        if (yCursorLine == null) {
            yCursorLine = RelativeLayout(context).apply {
                val color = ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor
                setBackgroundColor(Color.parseColor(color))
            }
            yCursorLine?.let {
                window.addView(
                    it,
                    xPosition,
                    y,
                    width,
                    ScanMethodUIConstants.LINE_THICKNESS
                )
            }
        } else {
            updateYCursorLine(y)
        }
    }

    /**
     * Shows or updates the horizontal quadrant.
     *
     * @param quadrantNumber The quadrant number to position the quadrant.
     */
    fun showXQuadrant(quadrantNumber: Int) {
        val yPosition = CursorBounds.Y_MIN
        val height = CursorBounds.height(context)
        val xPosition = quadrantNumber * getQuadrantWidth()

        if (xQuadrant == null) {
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
            xQuadrant?.let {
                window.addView(it, xPosition, yPosition, getQuadrantWidth(), height)
            }
        } else {
            updateXQuadrant(quadrantNumber)
        }
    }

    /**
     * Shows or updates the vertical quadrant.
     *
     * @param quadrantNumber The quadrant number to position the quadrant.
     */
    fun showYQuadrant(quadrantNumber: Int) {
        val xPosition = CursorBounds.X_MIN
        val width = CursorBounds.width(context)
        val yPosition = CursorBounds.Y_MIN + (quadrantNumber * getQuadrantHeight())

        if (yQuadrant == null) {
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
            yQuadrant?.let {
                window.addView(it, xPosition, yPosition, width, getQuadrantHeight())
            }
        } else {
            updateYQuadrant(quadrantNumber)
        }
    }

    /**
     * Removes the horizontal cursor line.
     */
    fun removeXCursorLine() {
        xCursorLine?.let {
            window.removeView(it)
            xCursorLine = null
        }
    }

    /**
     * Removes the vertical cursor line.
     */
    fun removeYCursorLine() {
        yCursorLine?.let {
            window.removeView(it)
            yCursorLine = null
        }
    }

    /**
     * Removes the horizontal quadrant.
     */
    fun removeXQuadrant() {
        xQuadrant?.let {
            window.removeView(it)
            xQuadrant = null
        }
    }

    /**
     * Removes the vertical quadrant.
     */
    fun removeYQuadrant() {
        yQuadrant?.let {
            window.removeView(it)
            yQuadrant = null
        }
    }

    /**
     * Updates the position of the horizontal cursor line.
     *
     * @param x The new x-coordinate for the line.
     */
    private fun updateXCursorLine(x: Int) {
        xCursorLine?.let {
            window.updateViewLayout(it, x, CursorBounds.Y_MIN)
        }
    }

    /**
     * Updates the position of the vertical cursor line.
     *
     * @param y The new y-coordinate for the line.
     */
    private fun updateYCursorLine(y: Int) {
        yCursorLine?.let {
            window.updateViewLayout(it, CursorBounds.X_MIN, y)
        }
    }

    /**
     * Updates the position of the horizontal quadrant.
     *
     * @param quadrantNumber The new quadrant number for positioning.
     */
    private fun updateXQuadrant(quadrantNumber: Int) {
        val quadrantWidth = getQuadrantWidth()
        val xPosition = quadrantNumber * quadrantWidth
        xQuadrant?.let {
            window.updateViewLayout(it, xPosition, CursorBounds.Y_MIN)
        }
    }

    /**
     * Updates the position of the vertical quadrant.
     *
     * @param quadrantNumber The new quadrant number for positioning.
     */
    private fun updateYQuadrant(quadrantNumber: Int) {
        val quadrantHeight = getQuadrantHeight()
        val yPosition = CursorBounds.Y_MIN + (quadrantNumber * quadrantHeight)
        yQuadrant?.let {
            window.updateViewLayout(it, CursorBounds.X_MIN, yPosition)
        }
    }

    /**
     * Checks if all cursor UI elements are removed.
     *
     * @return True if all elements are removed, false otherwise.
     */
    fun isReset(): Boolean {
        return xCursorLine == null && yCursorLine == null && xQuadrant == null && yQuadrant == null
    }

    /**
     * Removes all cursor UI elements.
     */
    fun reset() {
        removeXCursorLine()
        removeYCursorLine()
        removeXQuadrant()
        removeYQuadrant()
    }

    /**
     * Checks if the horizontal cursor line is visible.
     *
     * @return True if visible, false otherwise.
     */
    fun isXCursorLineVisible(): Boolean = xCursorLine != null

    /**
     * Checks if the vertical cursor line is visible.
     *
     * @return True if visible, false otherwise.
     */
    fun isYCursorLineVisible(): Boolean = yCursorLine != null

    /**
     * Checks if the horizontal quadrant is visible.
     *
     * @return True if visible, false otherwise.
     */
    fun isXQuadrantVisible(): Boolean = xQuadrant != null

    /**
     * Checks if the vertical quadrant is visible.
     *
     * @return True if visible, false otherwise.
     */
    fun isYQuadrantVisible(): Boolean = yQuadrant != null
}