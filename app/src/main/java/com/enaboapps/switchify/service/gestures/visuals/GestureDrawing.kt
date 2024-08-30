package com.enaboapps.switchify.service.gestures.visuals

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanColorManager
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * GestureDrawing class is responsible for drawing visual feedback for user interactions
 * using the SwitchifyAccessibilityWindow.
 *
 * @property context The context used to create views and access resources.
 */
class GestureDrawing(private val context: Context) {

    // Instance of SwitchifyAccessibilityWindow used to manage views
    private val switchifyAccessibilityWindow = SwitchifyAccessibilityWindow.instance

    /**
     * Draws a circle at the specified coordinates and removes it after a given time.
     *
     * @param x The x-coordinate of the circle's center.
     * @param y The y-coordinate of the circle's center.
     * @param time The duration in milliseconds for which the circle should be visible.
     */
    fun drawCircleAndRemove(
        x: Int,
        y: Int,
        time: Long,
    ) {
        val circleSize = 40

        // Create a circular drawable for the circle
        val gradientDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(
                Color.parseColor(
                    ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor
                )
            )
            setSize(circleSize, circleSize)
        }

        // Create an ImageView to display the circle
        val circle = ImageView(context).apply {
            setImageDrawable(gradientDrawable)
        }

        // Wrap the ImageView in a RelativeLayout for easier positioning
        val circleLayout = RelativeLayout(context).apply {
            addView(circle, RelativeLayout.LayoutParams(circleSize, circleSize))
        }

        // Add the circle to the accessibility window
        switchifyAccessibilityWindow.addView(
            circleLayout,
            x - circleSize / 2,
            y - circleSize / 2,
            circleSize,
            circleSize
        )

        // Remove the circle after the specified time
        Handler(Looper.getMainLooper()).postDelayed({
            switchifyAccessibilityWindow.removeView(circleLayout)
        }, time)
    }

    /**
     * Draws a line with an arrow from (x1, y1) to (x2, y2) and removes it after a given time.
     *
     * @param x1 The x-coordinate of the start point.
     * @param y1 The y-coordinate of the start point.
     * @param x2 The x-coordinate of the end point.
     * @param y2 The y-coordinate of the end point.
     * @param time The duration in milliseconds for which the line should be visible.
     */
    fun drawLineAndArrowAndRemove(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        time: Long,
    ) {
        // Create a GestureIndicatorView to draw the line and arrow
        val gestureIndicatorView = GestureIndicatorView(context)
        gestureIndicatorView.visibility = View.VISIBLE
        gestureIndicatorView.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        gestureIndicatorView.setGesture(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

        // Add the gesture indicator view to the center of the accessibility window
        switchifyAccessibilityWindow.addViewToCenter(gestureIndicatorView)

        // Remove the line after the specified time
        Handler(Looper.getMainLooper()).postDelayed({
            switchifyAccessibilityWindow.removeView(gestureIndicatorView)
        }, time)
    }
}