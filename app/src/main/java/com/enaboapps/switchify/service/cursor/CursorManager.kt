package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.utils.ScreenUtils
import com.enaboapps.switchify.service.window.SwitchifyHUD
import java.util.Timer
import java.util.TimerTask

class CursorManager(private val context: Context) {

    private val TAG = "CursorManager"

    private val cursorLineThickness = 10

    private val preferenceManager: PreferenceManager = PreferenceManager(context)

    private var xQuadrant: LinearLayout? = null
    private var yQuadrant: LinearLayout? = null

    private var xCursorLine: LinearLayout? = null
    private var yCursorLine: LinearLayout? = null

    private var isInQuadrant = false
    private var quadrantInfo: QuadrantInfo? = null

    private var x: Int = 0
    private var y: Int = 0

    private var direction: Direction = Direction.RIGHT

    private var movingTimer: Timer? = null // Timer to move the cursor line

    // auto select variables
    private var isInAutoSelect = false // If true, we listen for a second event to activate the menu
    private var autoSelectTimer: Timer? = null // Timer to wait for the second event

    private var switchifyHUD: SwitchifyHUD? = null

    enum class Direction {
        LEFT, RIGHT, UP, DOWN
    }


    fun setup() {
        switchifyHUD = SwitchifyHUD.getInstance(context)
    }


    private fun setupYQuadrant() {
        if (yQuadrant == null) {
            y = 0
            yQuadrant = LinearLayout(context)
            yQuadrant?.setBackgroundColor(Color.RED)
            yQuadrant?.alpha = 0.5f
            val width = ScreenUtils.getWidth(context)
            val height = ScreenUtils.getHeight(context) / 4
            switchifyHUD?.addView(yQuadrant!!, x, y, width, height)
        }
    }

    private fun setupXQuadrant() {
        if (xQuadrant == null) {
            x = 0
            xQuadrant = LinearLayout(context)
            xQuadrant?.setBackgroundColor(Color.RED)
            xQuadrant?.alpha = 0.5f
            val width = ScreenUtils.getWidth(context) / 4
            val height = ScreenUtils.getHeight(context)
            switchifyHUD?.addView(xQuadrant!!, x, y, width, height)
        }
    }


    private fun setupYCursorLine() {
        if (yCursorLine == null) {
            quadrantInfo = QuadrantInfo(y, y + ScreenUtils.getHeight(context) / 4)
            Log.d(TAG, "setupYCursorLine: $y")
            yCursorLine = LinearLayout(context)
            yCursorLine?.setBackgroundColor(Color.RED)
            val width = ScreenUtils.getWidth(context)
            val height = cursorLineThickness
            quadrantInfo?.start?.let { switchifyHUD?.addView(yCursorLine!!, x, it, width, height) }
        }
    }

    private fun setupXCursorLine() {
        if (xCursorLine == null) {
            quadrantInfo = QuadrantInfo(x, x + ScreenUtils.getWidth(context) / 4)
            Log.d(TAG, "setupXCursorLine: $x")
            xCursorLine = LinearLayout(context)
            xCursorLine?.setBackgroundColor(Color.RED)
            val width = cursorLineThickness
            val height = ScreenUtils.getHeight(context)
            quadrantInfo?.start?.let { switchifyHUD?.addView(xCursorLine!!, it, y, width, height) }
        }
    }


    private fun start() {
        var rate =
            preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        if (isInQuadrant && rate > 200) {
            rate = 200
        }
        Log.d(TAG, "start: $rate")
        val handler = Handler(Looper.getMainLooper())
        if (movingTimer == null) {
            movingTimer = Timer()
            movingTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (isInQuadrant) {
                            moveCursorLine()
                        } else {
                            moveToNextQuadrant()
                        }
                    }
                }
            }, rate.toLong(), rate.toLong())
        }
    }


    // Function to stop the timer
    private fun stop() {
        movingTimer?.cancel()
        movingTimer = null
    }


    // Function to move to the next quadrant
    private fun moveToNextQuadrant() {
        when (direction) {
            Direction.LEFT -> {
                if (x > 0) {
                    x -= ScreenUtils.getWidth(context) / 4
                    switchifyHUD?.updateViewLayout(xQuadrant!!, x, y)
                } else {
                    direction = Direction.RIGHT
                    moveToNextQuadrant()
                }
            }
            Direction.RIGHT -> {
                if (x < ScreenUtils.getWidth(context) - ScreenUtils.getWidth(context) / 4) {
                    x += ScreenUtils.getWidth(context) / 4
                    switchifyHUD?.updateViewLayout(xQuadrant!!, x, y)
                } else {
                    direction = Direction.LEFT
                    moveToNextQuadrant()
                }
            }
            Direction.UP -> {
                if (y > 0) {
                    y -= ScreenUtils.getHeight(context) / 4
                    switchifyHUD?.updateViewLayout(yQuadrant!!, x, y)
                } else {
                    direction = Direction.DOWN
                    moveToNextQuadrant()
                }
            }
            Direction.DOWN -> {
                if (y < ScreenUtils.getHeight(context) - ScreenUtils.getHeight(context) / 4) {
                    y += ScreenUtils.getHeight(context) / 4
                    switchifyHUD?.updateViewLayout(yQuadrant!!, x, y)
                } else {
                    direction = Direction.UP
                    moveToNextQuadrant()
                }
            }
        }
    }


    // Function to move the cursor line
    private fun moveCursorLine() {
        if (quadrantInfo != null) {
            when (direction) {
                Direction.LEFT ->
                    if (x > quadrantInfo?.start!!) {
                        x -= cursorLineThickness * 2
                        switchifyHUD?.updateViewLayout(xCursorLine!!, x, y)
                    } else {
                        direction = Direction.RIGHT
                        moveCursorLine()
                    }
                Direction.RIGHT ->
                    if (x < quadrantInfo?.end!!) {
                        x += cursorLineThickness * 2
                        switchifyHUD?.updateViewLayout(xCursorLine!!, x, y)
                    } else {
                        direction = Direction.LEFT
                        moveCursorLine()
                    }
                Direction.UP ->
                    if (y > quadrantInfo?.start!!) {
                        y -= cursorLineThickness * 2
                        switchifyHUD?.updateViewLayout(yCursorLine!!, x, y)
                    } else {
                        direction = Direction.DOWN
                        moveCursorLine()
                    }
                Direction.DOWN ->
                    if (y < quadrantInfo?.end!!) {
                        y += cursorLineThickness * 2
                        switchifyHUD?.updateViewLayout(yCursorLine!!, x, y)
                    } else {
                        direction = Direction.UP
                        moveCursorLine()
                    }
            }
        }
    }


    fun reset() {
        stop()

        x = 0
        y = 0

        direction = Direction.RIGHT

        resetQuadrants()
        resetCursorLines()
    }

    private fun resetQuadrants() {
        xQuadrant?.let {
            switchifyHUD?.removeView(it)
        }
        yQuadrant?.let {
            switchifyHUD?.removeView(it)
        }
        xQuadrant = null
        yQuadrant = null
    }

    private fun resetCursorLines() {
        xCursorLine?.let {
            switchifyHUD?.removeView(it)
        }
        yCursorLine?.let {
            switchifyHUD?.removeView(it)
        }
        xCursorLine = null
        yCursorLine = null
    }


    fun performAction() {
        // If the event is triggered within the auto select delay, we don't perform the action
        if (checkAutoSelectDelay()) {
            return
        }

        // If moving timer is null, we start the timer and return
        if (movingTimer == null) {
            setupXQuadrant()
            start()
            return
        }

        // We perform the action based on the direction
        when (direction) {
            Direction.LEFT, Direction.RIGHT -> {
                stop()
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = Direction.RIGHT

                    resetQuadrants()

                    if (xCursorLine == null) {
                        setupXCursorLine()
                    }
                } else {
                    direction = Direction.DOWN
                    isInQuadrant = false

                    if (xQuadrant == null) {
                        setupYQuadrant()
                    }
                }
                start()
            }
            Direction.UP, Direction.DOWN -> {
                stop()
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = Direction.DOWN

                    resetQuadrants()

                    if (yCursorLine == null) {
                        setupYCursorLine()
                    }

                    start()
                } else {
                    isInQuadrant = false

                    performFinalAction()
                }
            }
        }
    }


    private fun performFinalAction() {
        // get the point
        val point = PointF(
            (x + (cursorLineThickness / 2)).toFloat(),
            (y + (cursorLineThickness / 2)).toFloat()
        )
        GestureManager.getInstance().currentPoint = point

        // check if auto select is enabled, if so, start the timer
        val auto = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
        if (auto && !isInAutoSelect) {
            startAutoSelectTimer()
        }

        if (!auto) {
            // open menu
            MenuManager.getInstance().openMainMenu()
        }

        reset()
    }


    // Function to check if the event is triggered within the auto select delay
    private fun checkAutoSelectDelay(): Boolean {
        if (isInAutoSelect) {
            Log.d(TAG, "checkAutoSelectDelay: true")
            isInAutoSelect = false
            autoSelectTimer?.cancel()
            autoSelectTimer = null
            // open menu
            MenuManager.getInstance().openMainMenu()
            return true
        }
        return false
    }


    // Function to start auto select timer
    private fun startAutoSelectTimer() {
        val delay =
            preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
        val handler = Handler(Looper.getMainLooper())
        isInAutoSelect = true
        if (autoSelectTimer == null) {
            autoSelectTimer = Timer()
        }
        autoSelectTimer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    if (isInAutoSelect) {
                        isInAutoSelect = false
                        // tap
                        GestureManager.getInstance().performTap()
                    }
                }
            }
        }, delay.toLong())
    }

}

data class QuadrantInfo(
    val start: Int,
    val end: Int,
)