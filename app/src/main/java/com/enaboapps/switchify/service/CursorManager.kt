package com.enaboapps.switchify.service

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import java.util.*

class CursorManager(private val context: Context) {

    private val TAG = "CursorManager"

    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var layout: LinearLayout? = null

    private var x: Int = 0
    private var y: Int = 0

    private var direction: Direction = Direction.RIGHT

    private var timer: Timer? = null

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }




    // function to setup the layout
    fun setupLayout() {
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

        layout = LinearLayout(context)
        layout!!.setBackgroundColor(Color.GREEN and 0x55FFFFFF)

        layoutParams = WindowManager.LayoutParams()
        layoutParams?.apply {
            y = 0
            x = 0
            width = 10
            height = getScreenSize().y
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            gravity = Gravity.TOP or Gravity.LEFT
            format = PixelFormat.TRANSPARENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        try {
            windowManager!!.addView(layout, layoutParams)
        } catch (ex: Exception) {
            Log.e(TAG, "adding view failed", ex)
        }
    }

    fun start() {
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




    private fun move() {
        when (direction) {
            Direction.LEFT ->
                if (x > 0) {
                    x -= 10
                } else {
                    direction = Direction.RIGHT
                }
            Direction.RIGHT ->
                if (x < getScreenSize().x) {
                    x += 10
                } else {
                    direction = Direction.LEFT
                }
            Direction.UP ->
                if (y > 0) {
                    y -= 10
                } else {
                    direction = Direction.DOWN
                }
            Direction.DOWN ->
                if (y < getScreenSize().y) {
                    y += 10
                } else {
                    direction = Direction.UP
                }
        }
        layoutParams?.x = x
        layoutParams?.y = y
        windowManager?.updateViewLayout(layout, layoutParams)
    }



    fun stop() {
        timer?.cancel()
        timer = null
    }


    private fun reset() {
        x = 0
        y = 0
        layoutParams?.x = x
        layoutParams?.y = y
        layoutParams?.width = 10
        layoutParams?.height = getScreenSize().y
        windowManager?.updateViewLayout(layout, layoutParams)
    }



    fun performAction() {
        when (direction) {
            Direction.LEFT, Direction.RIGHT -> {
                direction = Direction.DOWN
                layoutParams?.y = 0
                layoutParams?.x = 0
                layoutParams?.width = getScreenSize().y
                layoutParams?.height = 10
                windowManager?.updateViewLayout(layout, layoutParams)
            }
            Direction.UP, Direction.DOWN ->
                performTap()
        }
    }




    fun performTap() {
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