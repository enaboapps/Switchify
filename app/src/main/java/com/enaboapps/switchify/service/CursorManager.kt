package com.enaboapps.switchify.service

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import java.util.*

interface TapGestureListener {
    fun onTap(point: PointF)
}

class CursorManager(private val context: Context) {

    private val TAG = "CursorManager"

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
    }



    private fun setupYLayout() {
        if (yLayout == null) {
            yLayout = LinearLayout(context)
            yLayout!!.setBackgroundColor(Color.RED)
            yLayoutParams = WindowManager.LayoutParams()
            yLayoutParams?.width = getScreenSize().x
            yLayoutParams?.height = 10
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
            xLayout!!.setBackgroundColor(Color.RED)
            xLayoutParams = WindowManager.LayoutParams()
            xLayoutParams?.x = 0
            xLayoutParams?.y = 0
            xLayoutParams?.width = 10
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
            timer!!.scheduleAtFixedRate(object : TimerTask() {
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
                    x -= 10
                    xLayoutParams!!.x = x
                    windowManager!!.updateViewLayout(xLayout, xLayoutParams)
                } else {
                    direction = Direction.RIGHT
                }
            Direction.RIGHT ->
                if (x < getScreenSize().x) {
                    x += 10
                    xLayoutParams!!.x = x
                    windowManager!!.updateViewLayout(xLayout, xLayoutParams)
                } else {
                    direction = Direction.LEFT
                }
            Direction.UP ->
                if (y > 0) {
                    y -= 10
                    yLayoutParams!!.y = y
                    windowManager!!.updateViewLayout(yLayout, yLayoutParams)
                } else {
                    direction = Direction.DOWN
                }
            Direction.DOWN ->
                if (y < getScreenSize().y) {
                    y += 10
                    yLayoutParams!!.y = y
                    windowManager!!.updateViewLayout(yLayout, yLayoutParams)
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

        xLayoutParams = null
        xLayout = null
        yLayoutParams = null
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




    fun performTap() {
        tapGestureListener?.onTap(PointF(x.toFloat(), y.toFloat()))
        reset()
    }




    // function to get screen size
    private fun getScreenSize(): Point {
        val display = windowManager!!.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

}