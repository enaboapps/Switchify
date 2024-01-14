package com.enaboapps.switchify.service.gestures

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import kotlin.math.abs

class GestureDrawing(private val context: Context) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager


    // Function to draw a circle at x, y and remove after half a second
    fun drawCircleAndRemove(
        x: Int,
        y: Int,
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
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.addView(circle, layoutParams)

        // Remove the circle after half a second
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager.removeView(circle)
        }, 500)
    }

    // Function to draw a line from x1, y1 to x2, y2 and remove after half a second
    fun drawLineAndRemove(
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
    ) {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        gradientDrawable.setColor(Color.RED)

        val requiredHeightOrWidth = 40

        val line = ImageView(context)
        line.setImageDrawable(gradientDrawable)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.x = if (x1 < x2) x1 else x2
        layoutParams.y = if (y1 < y2) y1 else y2
        layoutParams.width = if (x1 == x2) requiredHeightOrWidth else abs(x1 - x2)
        layoutParams.height = if (y1 == y2) requiredHeightOrWidth else abs(y1 - y2)
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.format = PixelFormat.TRANSPARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.addView(line, layoutParams)

        // Remove the line after half a second
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager.removeView(line)
        }, 500)
    }
}