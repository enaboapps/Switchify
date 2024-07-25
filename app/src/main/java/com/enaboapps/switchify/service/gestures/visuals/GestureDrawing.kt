package com.enaboapps.switchify.service.gestures.visuals

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView

class GestureDrawing(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager


    // Function to draw a circle at x, y and remove after a specified time
    fun drawCircleAndRemove(
        x: Int,
        y: Int,
        time: Long,
    ) {
        val circleSize = 40

        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setColor(Color.RED)
        gradientDrawable.setSize(circleSize, circleSize)

        val circle = ImageView(context)
        circle.setImageDrawable(gradientDrawable)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.x = x - circleSize / 2
        layoutParams.y = y - circleSize / 2
        layoutParams.width = circleSize
        layoutParams.height = circleSize
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.format = PixelFormat.TRANSPARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        layoutParams.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        windowManager.addView(circle, layoutParams)

        // Remove the circle after the specified time
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager.removeView(circle)
        }, time)
    }

    // Function to draw a line from x1, y1 to x2, y2
    // and add an arrow at the end of the line
    // and remove the line after a specified time
    fun drawLineAndArrowAndRemove(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        time: Long,
    ) {
        val gestureIndicatorView = GestureIndicatorView(context)
        gestureIndicatorView.setGesture(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.x = 0
        layoutParams.y = 0
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.format = PixelFormat.TRANSPARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        layoutParams.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        windowManager.addView(gestureIndicatorView, layoutParams)

        // Remove the line after the specified time
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager.removeView(gestureIndicatorView)
        }, time)
    }
}