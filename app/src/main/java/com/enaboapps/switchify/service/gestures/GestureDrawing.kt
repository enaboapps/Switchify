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
        layoutParams.x = x
        layoutParams.y = y
        layoutParams.width = circleSize
        layoutParams.height = circleSize
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.format = PixelFormat.TRANSPARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager?.addView(circle, layoutParams)

        // Remove the circle after half a second
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager?.removeView(circle)
        }, 500)
    }
}