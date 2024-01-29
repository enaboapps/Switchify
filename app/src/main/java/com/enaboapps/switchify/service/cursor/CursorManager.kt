package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RelativeLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.service.scanning.ScanState
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.utils.ScreenUtils
import java.util.Timer
import java.util.TimerTask

class CursorManager(private val context: Context) : ScanStateInterface {

    private val TAG = "CursorManager"

    private val cursorLineThickness = 10
    private val cursorLineMovement = (cursorLineThickness * 4)

    private val preferenceManager: PreferenceManager = PreferenceManager(context)

    private var xQuadrant: RelativeLayout? = null
    private var yQuadrant: RelativeLayout? = null

    private var xCursorLine: RelativeLayout? = null
    private var yCursorLine: RelativeLayout? = null

    private var isInQuadrant = false
    private var quadrantInfo: QuadrantInfo? = null

    private var x: Int = 0
    private var y: Int = 0

    private var scanState = ScanState.STOPPED

    private var direction: ScanDirection = ScanDirection.RIGHT

    private var movingTimer: Timer? = null // Timer to move the cursor line

    // auto select variables
    private var isInAutoSelect = false // If true, we listen for a second event to activate the menu
    private var autoSelectTimer: Timer? = null // Timer to wait for the second event

    // Handler to update the UI
    private val uiHandler = Handler(Looper.getMainLooper())

    private var cursorHUD: CursorHUD? = null


    fun setup() {
        cursorHUD = CursorHUD(context)
        cursorHUD?.show()
    }


    // Function to set quadrant info
    // Takes the quadrant index and the start and end points of the quadrant
    private fun setQuadrantInfo(quadrantIndex: Int, start: Int, end: Int) {
        quadrantInfo = QuadrantInfo(quadrantIndex, start, end)
    }


    private fun setupYQuadrant() {
        if (yQuadrant == null) {
            y = 0
            yQuadrant = RelativeLayout(context)
            yQuadrant?.setBackgroundColor(Color.RED)
            yQuadrant?.alpha = 0.5f
            val width = ScreenUtils.getWidth(context)
            val height = ScreenUtils.getHeight(context) / 4
            cursorHUD?.addView(yQuadrant!!, 0, y, width, height)
            setQuadrantInfo(0, y, y + height)
        }
    }

    private fun updateYQuadrant(quadrantIndex: Int) {
        yQuadrant?.let {
            uiHandler.post {
                y = quadrantIndex * ScreenUtils.getHeight(context) / 4
                cursorHUD?.updateViewLayout(it, 0, y)
                setQuadrantInfo(quadrantIndex, y, y + ScreenUtils.getHeight(context) / 4)
            }
        }
    }

    private fun setupXQuadrant() {
        if (xQuadrant == null) {
            x = 0
            xQuadrant = RelativeLayout(context)
            xQuadrant?.setBackgroundColor(Color.RED)
            xQuadrant?.alpha = 0.5f
            val width = ScreenUtils.getWidth(context) / 4
            val height = ScreenUtils.getHeight(context)
            cursorHUD?.addView(xQuadrant!!, x, y, width, height)
            setQuadrantInfo(0, x, x + width)
        }
    }

    private fun updateXQuadrant(quadrantIndex: Int) {
        xQuadrant?.let {
            uiHandler.post {
                x = quadrantIndex * ScreenUtils.getWidth(context) / 4
                cursorHUD?.updateViewLayout(it, x, y)
                setQuadrantInfo(quadrantIndex, x, x + ScreenUtils.getWidth(context) / 4)
            }
        }
    }


    private fun setupYCursorLine() {
        if (yCursorLine == null) {
            Log.d(TAG, "setupYCursorLine: $y")
            yCursorLine = RelativeLayout(context)
            yCursorLine?.setBackgroundColor(Color.RED)
            val width = ScreenUtils.getWidth(context)
            val height = cursorLineThickness
            quadrantInfo?.start?.let { cursorHUD?.addView(yCursorLine!!, 0, it, width, height) }
        }
    }

    private fun updateYCursorLine() {
        yCursorLine?.let {
            cursorHUD?.updateViewLayout(it, 0, y)
        }
    }

    private fun setupXCursorLine() {
        if (xCursorLine == null) {
            Log.d(TAG, "setupXCursorLine: $x")
            xCursorLine = RelativeLayout(context)
            xCursorLine?.setBackgroundColor(Color.RED)
            val width = cursorLineThickness
            val height = ScreenUtils.getHeight(context)
            quadrantInfo?.start?.let { cursorHUD?.addView(xCursorLine!!, it, y, width, height) }
        }
    }

    private fun updateXCursorLine() {
        xCursorLine?.let {
            cursorHUD?.updateViewLayout(it, x, y)
        }
    }


    private fun start() {
        scanState = ScanState.SCANNING

        val mode = ScanMode(preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
        if (mode.id == ScanMode.Modes.MODE_MANUAL) {
            return
        }

        var rate =
            preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        if (isInQuadrant) {
            rate =
                preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
        }
        Log.d(TAG, "start: $rate")
        val handler = Handler(Looper.getMainLooper())
        if (movingTimer == null) {
            movingTimer = Timer()
            movingTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        move()
                    }
                }
            }, rate.toLong(), rate.toLong())
        }
    }


    // Function to swap the direction
    fun swapDirection() {
        // Here we swap the direction
        // If we are at the first quadrant, we swap the direction and go to the last quadrant, and vice versa
        when (direction) {
            ScanDirection.LEFT -> {
                direction = ScanDirection.RIGHT
                if (!isInQuadrant) {
                    quadrantInfo?.let {
                        if (it.quadrantIndex == MIN_QUADRANT_INDEX) {
                            updateXQuadrant(MAX_QUADRANT_INDEX)
                        } else if (it.quadrantIndex == MAX_QUADRANT_INDEX) {
                            updateXQuadrant(MIN_QUADRANT_INDEX)
                        }
                    }
                }
            }
            ScanDirection.RIGHT -> {
                direction = ScanDirection.LEFT
                if (!isInQuadrant) {
                    quadrantInfo?.let {
                        if (it.quadrantIndex == MIN_QUADRANT_INDEX) {
                            updateXQuadrant(MAX_QUADRANT_INDEX)
                        } else if (it.quadrantIndex == MAX_QUADRANT_INDEX) {
                            updateXQuadrant(MIN_QUADRANT_INDEX)
                        }
                    }
                }
            }
            ScanDirection.UP -> {
                direction = ScanDirection.DOWN
                if (!isInQuadrant) {
                    quadrantInfo?.let {
                        if (it.quadrantIndex == MIN_QUADRANT_INDEX) {
                            updateYQuadrant(MAX_QUADRANT_INDEX)
                        } else if (it.quadrantIndex == MAX_QUADRANT_INDEX) {
                            updateYQuadrant(MIN_QUADRANT_INDEX)
                        }
                    }
                }
            }
            ScanDirection.DOWN -> {
                direction = ScanDirection.UP
                if (!isInQuadrant) {
                    quadrantInfo?.let {
                        if (it.quadrantIndex == MIN_QUADRANT_INDEX) {
                            updateYQuadrant(MAX_QUADRANT_INDEX)
                        } else if (it.quadrantIndex == MAX_QUADRANT_INDEX) {
                            updateYQuadrant(MIN_QUADRANT_INDEX)
                        }
                    }
                }
            }
        }
    }


    // Function to stop the timer
    override fun stopScanning() {
        if (scanState == ScanState.SCANNING) {
            scanState = ScanState.STOPPED
            movingTimer?.cancel()
            movingTimer = null
        }
    }


    // Function to pause the scanning
    override fun pauseScanning() {
        if (scanState == ScanState.SCANNING) {
            scanState = ScanState.PAUSED
        }
    }


    // Function to resume the scanning
    override fun resumeScanning() {
        if (scanState == ScanState.PAUSED) {
            scanState = ScanState.SCANNING
        }
    }


    private fun move() {
        if (scanState == ScanState.SCANNING) {
            if (isInQuadrant) {
                moveCursorLine()
            } else {
                moveToNextQuadrant()
            }
            Log.d(TAG, "move: $x, $y, $direction")
        }
    }


    // Function to move to the next quadrant
    private fun moveToNextQuadrant() {
        when (direction) {
            ScanDirection.LEFT -> {
                quadrantInfo?.let {
                    if (it.quadrantIndex > MIN_QUADRANT_INDEX) {
                        val quadrantIndex = it.quadrantIndex - 1
                        updateXQuadrant(quadrantIndex)
                    } else {
                        direction = ScanDirection.RIGHT
                        moveToNextQuadrant()
                    }
                }
            }

            ScanDirection.RIGHT -> {
                quadrantInfo?.let {
                    if (it.quadrantIndex < MAX_QUADRANT_INDEX) {
                        val quadrantIndex = it.quadrantIndex + 1
                        updateXQuadrant(quadrantIndex)
                    } else {
                        direction = ScanDirection.LEFT
                        moveToNextQuadrant()
                    }
                }
            }

            ScanDirection.UP -> {
                quadrantInfo?.let {
                    if (it.quadrantIndex > MIN_QUADRANT_INDEX) {
                        val quadrantIndex = it.quadrantIndex - 1
                        updateYQuadrant(quadrantIndex)
                    } else {
                        direction = ScanDirection.DOWN
                        moveToNextQuadrant()
                    }
                }
            }

            ScanDirection.DOWN -> {
                quadrantInfo?.let {
                    if (it.quadrantIndex < MAX_QUADRANT_INDEX) {
                        val quadrantIndex = it.quadrantIndex + 1
                        updateYQuadrant(quadrantIndex)
                    } else {
                        direction = ScanDirection.UP
                        moveToNextQuadrant()
                    }
                }
            }
        }
    }


    // Function to move the cursor line
    private fun moveCursorLine() {
        if (quadrantInfo != null) {
            when (direction) {
                ScanDirection.LEFT ->
                    if (x > quadrantInfo?.start!!) {
                        x -= cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.RIGHT
                        moveCursorLine()
                    }

                ScanDirection.RIGHT ->
                    if (x < quadrantInfo?.end!!) {
                        x += cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.LEFT
                        moveCursorLine()
                    }

                ScanDirection.UP ->
                    if (y > quadrantInfo?.start!!) {
                        y -= cursorLineMovement
                        updateYCursorLine()
                    } else {
                        direction = ScanDirection.DOWN
                        moveCursorLine()
                    }

                ScanDirection.DOWN ->
                    if (y < quadrantInfo?.end!!) {
                        y += cursorLineMovement
                        updateYCursorLine()
                    } else {
                        direction = ScanDirection.UP
                        moveCursorLine()
                    }
            }
        }
    }


    fun externalReset() {
        uiHandler.post {
            internalReset()

            isInAutoSelect = false
            autoSelectTimer?.cancel()
            autoSelectTimer = null

            isInQuadrant = false
            quadrantInfo = null
        }
    }


    private fun internalReset() {
        stopScanning()

        x = 0
        y = 0

        direction = ScanDirection.RIGHT

        resetQuadrants()
        resetCursorLines()
    }

    private fun isReset(): Boolean {
        return xQuadrant == null && yQuadrant == null && xCursorLine == null && yCursorLine == null
    }

    private fun resetQuadrants() {
        xQuadrant?.let {
            cursorHUD?.removeView(it)
        }
        yQuadrant?.let {
            cursorHUD?.removeView(it)
        }
        xQuadrant = null
        yQuadrant = null
    }

    private fun resetCursorLines() {
        xCursorLine?.let {
            cursorHUD?.removeView(it)
        }
        yCursorLine?.let {
            cursorHUD?.removeView(it)
        }
        xCursorLine = null
        yCursorLine = null
    }


    fun moveToNextItem() {
        if (isReset()) {
            setupXQuadrant()
            start()
            return
        }

        direction = when (direction) {
            // If left or right, set right
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                ScanDirection.RIGHT
            }
            // If up or down, set down
            ScanDirection.UP, ScanDirection.DOWN -> {
                ScanDirection.DOWN
            }
        }

        if (isInQuadrant) {
            moveCursorLine()
        } else {
            moveToNextQuadrant()
        }
    }


    fun moveToPreviousItem() {
        if (isReset()) {
            setupXQuadrant()
            start()
            return
        }

        direction = when (direction) {
            // If left or right, set left
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                ScanDirection.LEFT
            }
            // If up or down, set up
            ScanDirection.UP, ScanDirection.DOWN -> {
                ScanDirection.UP
            }
        }

        if (isInQuadrant) {
            moveCursorLine()
        } else {
            moveToNextQuadrant()
        }
    }


    fun performSelectionAction() {
        // If the event is triggered within the auto select delay, we don't perform the action
        if (checkAutoSelectDelay()) {
            return
        }

        if (isReset()) {
            setupXQuadrant()
            start()
            return
        }

        // We perform the action based on the direction
        when (direction) {
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                stopScanning()
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = ScanDirection.RIGHT

                    resetQuadrants()

                    if (xCursorLine == null) {
                        setupXCursorLine()
                    }
                } else {
                    direction = ScanDirection.DOWN
                    isInQuadrant = false

                    if (xQuadrant == null) {
                        setupYQuadrant()
                    }
                }
                start()
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                stopScanning()
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = ScanDirection.DOWN

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
        val point = PointF(x.toFloat(), y.toFloat())
        GestureManager.getInstance().currentPoint = point

        // check if auto select is enabled, if so, start the timer
        val auto =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
        if (auto && !isInAutoSelect) {
            startAutoSelectTimer()
        }

        if (!auto) {
            // open menu
            MenuManager.getInstance().openMainMenu()
        }

        internalReset()
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
        isInAutoSelect = true
        if (autoSelectTimer == null) {
            autoSelectTimer = Timer()
        }
        autoSelectTimer?.schedule(object : TimerTask() {
            override fun run() {
                uiHandler.post {
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
    val quadrantIndex: Int,
    val start: Int,
    val end: Int,
)

val MIN_QUADRANT_INDEX = 0
val MAX_QUADRANT_INDEX = 3