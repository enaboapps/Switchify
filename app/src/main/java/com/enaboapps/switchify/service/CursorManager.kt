package com.enaboapps.switchify.service

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import java.util.*

interface TapGestureListener {
    fun onTap(point: PointF)
}

class CursorManager(private val context: Context) {

    private val TAG = "CursorManager"

    private val cursorLineThickness = 10

    public var tapGestureListener: TapGestureListener? = null

    private var windowManager: WindowManager? = null

    private var xLayoutParams: WindowManager.LayoutParams? = null
    private var xLayout: LinearLayout? = null
    private var yLayoutParams: WindowManager.LayoutParams? = null
    private var yLayout: LinearLayout? = null

    private var x: Int = 0
    private var y: Int = 0

    private var direction: Direction = Direction.RIGHT

    private var timer: Timer? = null

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }



    fun setup() {
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        xLayoutParams = WindowManager.LayoutParams()
        yLayoutParams = WindowManager.LayoutParams()
    }



    private fun setupYLayout() {
        if (yLayout == null) {
            yLayout = LinearLayout(context)
            yLayout?.setBackgroundColor(Color.RED)
            yLayoutParams?.width = getScreenSize().x
            yLayoutParams?.height = cursorLineThickness
            yLayoutParams?.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            yLayoutParams?.gravity = Gravity.TOP or Gravity.LEFT
            yLayoutParams?.format = PixelFormat.TRANSPARENT
            yLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager?.addView(yLayout, yLayoutParams)
        }
    }

    private fun setupXLayout() {
        if (xLayout == null) {
            xLayout = LinearLayout(context)
            xLayout?.setBackgroundColor(Color.RED)
            xLayoutParams?.x = 0
            xLayoutParams?.y = 0
            xLayoutParams?.width = cursorLineThickness
            xLayoutParams?.height = getScreenSize().y
            xLayoutParams?.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            xLayoutParams?.gravity = Gravity.TOP or Gravity.LEFT
            xLayoutParams?.format = PixelFormat.TRANSPARENT
            xLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager?.addView(xLayout, xLayoutParams)
        }
    }




    private fun start() {
        val handler = Handler(Looper.getMainLooper())
        if (timer == null) {
            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        move()
                    }
                }
            }, 0, 100)
        }
    }



    // Function to stop the timer
    private fun stop() {
        timer?.cancel()
        timer = null
    }




    // Function to move the cursor
    private fun move() {
        when (direction) {
            Direction.LEFT ->
                if (x > 0) {
                    if (xLayout != null) {
                        x -= 10
                        xLayoutParams?.x = x
                        windowManager?.updateViewLayout(xLayout, xLayoutParams)
                    }
                } else {
                    direction = Direction.RIGHT
                }
            Direction.RIGHT ->
                if (x < getScreenSize().x) {
                    if (xLayout != null) {
                        x += 10
                        xLayoutParams?.x = x
                        windowManager?.updateViewLayout(xLayout, xLayoutParams)
                    }
                } else {
                    direction = Direction.LEFT
                }
            Direction.UP ->
                if (y > 0) {
                    if (yLayout != null) {
                        y -= 10
                        yLayoutParams?.y = y
                        windowManager?.updateViewLayout(yLayout, yLayoutParams)
                    }
                } else {
                    direction = Direction.DOWN
                }
            Direction.DOWN ->
                if (y < getScreenSize().y) {
                    if (yLayout != null) {
                        y += 10
                        yLayoutParams?.y = y
                        windowManager?.updateViewLayout(yLayout, yLayoutParams)
                    }
                } else {
                    direction = Direction.UP
                }
        }
    }

    private fun reset() {
        stop()

        x = 0
        y = 0

        direction = Direction.RIGHT

        if (xLayout != null) {
            windowManager?.removeView(xLayout)
        }
        if (yLayout != null) {
            windowManager?.removeView(yLayout)
        }

        xLayout = null
        yLayout = null
    }



    fun performAction() {
        if (timer == null) {
            setupXLayout()
            start()
            return
        }
        when (direction) {
            Direction.LEFT, Direction.RIGHT -> {
                direction = Direction.DOWN
                if (yLayout == null) {
                    setupYLayout()
                }
            }
            Direction.UP, Direction.DOWN ->
                performTap()
        }
    }




    private fun performTap() {
        val point = PointF((x + (cursorLineThickness / 2)).toFloat(), (y + (cursorLineThickness / 2)).toFloat())
        tapGestureListener?.onTap(point)
        drawCircleAndRemove()
        reset()
    }



    // Function to draw a circle at x, y and remove after half a second
    private fun drawCircleAndRemove() {
        val circleSize = cursorLineThickness * 2

        val gradientDrawable = GradientDrawable()
        gradientDrawable.shape = GradientDrawable.OVAL
        gradientDrawable.setColor(Color.RED)
        gradientDrawable.setSize(circleSize, circleSize)

        val circle = ImageView(context)
        circle.setImageDrawable(gradientDrawable)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.x = x - cursorLineThickness / 2
        layoutParams.y = y - cursorLineThickness / 2
        layoutParams.width = circleSize
        layoutParams.height = circleSize
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        layoutParams.gravity = Gravity.TOP or Gravity.LEFT
        layoutParams.format = PixelFormat.TRANSPARENT
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager?.addView(circle, layoutParams)

        // Remove the circle after half a second
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            windowManager?.removeView(circle)
        }, 500)
    }




    // function to get screen size
    private fun getScreenSize(): Point {
        val display = windowManager?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size
    }

}