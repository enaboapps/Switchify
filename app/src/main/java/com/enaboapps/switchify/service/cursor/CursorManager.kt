package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import java.util.Timer
import java.util.TimerTask

class CursorManager(private val context: Context) : ScanStateInterface, CursorPointListener {

    private val TAG = "CursorManager"

    private val cursorLineMovement = 40

    private val preferenceManager: PreferenceManager = PreferenceManager(context)

    private val uiHandler = Handler(Looper.getMainLooper())
    private val cursorUI = CursorUI(context, uiHandler)

    private var isInQuadrant = false
    private var quadrantInfo: QuadrantInfo? = null

    private var x: Int = 0
    private var y: Int = 0

    private var direction: ScanDirection = ScanDirection.RIGHT

    private val scanningScheduler = ScanningScheduler { move() }

    // auto select variables
    private var isInAutoSelect = false // If true, we listen for a second event to activate the menu
    private var autoSelectTimer: Timer? = null // Timer to wait for the second event


    fun setup() {
        SwitchifyAccessibilityWindow.instance.setup(context)
        SwitchifyAccessibilityWindow.instance.show()

        CursorPoint.instance.listener = this
    }


    override fun onCursorPointReselect() {
        y = 0
        // find the last quadrant
        quadrantInfo = CursorPoint.instance.lastXQuadrant
        x = quadrantInfo?.start!!
        isInQuadrant = true
        setupXCursorLine()
        start()
    }


    // Function to set quadrant info
    // Takes the quadrant index and the start and end points of the quadrant
    private fun setQuadrantInfo(quadrantIndex: Int, start: Int, end: Int) {
        quadrantInfo = QuadrantInfo(quadrantIndex, start, end)

        when (direction) {
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                CursorPoint.instance.lastXQuadrant = quadrantInfo!!
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                CursorPoint.instance.lastYQuadrant = quadrantInfo!!
            }
        }
    }


    private fun setupYQuadrant() {
        y = 0
        cursorUI.createYQuadrant(0)
        setQuadrantInfo(0, y, y + cursorUI.getQuadrantHeight())
    }

    private fun updateYQuadrant(quadrantIndex: Int) {
        y = quadrantIndex * cursorUI.getQuadrantHeight()
        cursorUI.updateYQuadrant(quadrantIndex)
        setQuadrantInfo(quadrantIndex, y, y + cursorUI.getQuadrantHeight())
    }

    private fun setupXQuadrant() {
        x = 0
        cursorUI.createXQuadrant(0)
        setQuadrantInfo(0, x, x + cursorUI.getQuadrantWidth())
    }

    private fun updateXQuadrant(quadrantIndex: Int) {
        x = quadrantIndex * cursorUI.getQuadrantWidth()
        cursorUI.updateXQuadrant(quadrantIndex)
        setQuadrantInfo(quadrantIndex, x, x + cursorUI.getQuadrantWidth())
    }


    private fun setupYCursorLine() {
        quadrantInfo?.quadrantIndex?.let {
            cursorUI.createYCursorLine(it)
        }
    }

    private fun updateYCursorLine() {
        cursorUI.updateYCursorLine(y)
    }

    private fun setupXCursorLine() {
        quadrantInfo?.quadrantIndex?.let {
            cursorUI.createXCursorLine(it)
        }
    }

    private fun updateXCursorLine() {
        cursorUI.updateXCursorLine(x)
    }


    private fun start() {
        val mode =
            ScanMode(preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
        if (mode.id == ScanMode.Modes.MODE_MANUAL) {
            return
        }

        var rate =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        if (isInQuadrant) {
            rate =
                preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
        }
        Log.d(TAG, "start: $rate")
        scanningScheduler.startScanning(initialDelay = rate, period = rate)
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

        // Resume the scanning
        resumeScanning()
    }


    // Function to stop the scanning
    override fun stopScanning() {
        scanningScheduler.stopScanning()
    }


    // Function to pause the scanning
    override fun pauseScanning() {
        scanningScheduler.pauseScanning()
    }


    // Function to resume the scanning
    override fun resumeScanning() {
        scanningScheduler.resumeScanning()
    }


    private fun move() {
        if (isInQuadrant) {
            moveCursorLine()
        } else {
            moveToNextQuadrant()
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

        x = CursorPoint.instance.getRectForScreen(context).left
        y = CursorPoint.instance.getRectForScreen(context).top

        direction = ScanDirection.RIGHT

        cursorUI.reset()
    }

    private fun isReset(): Boolean {
        return cursorUI.isReset()
    }

    private fun resetQuadrants() {
        cursorUI.removeXQuadrant()
        cursorUI.removeYQuadrant()
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

        move()
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

        move()
    }


    fun performSelectionAction() {
        stopScanning()

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
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = ScanDirection.RIGHT

                    resetQuadrants()

                    if (!cursorUI.isXCursorLineVisible()) {
                        setupXCursorLine()
                    }
                } else {
                    direction = ScanDirection.DOWN
                    isInQuadrant = false

                    if (!cursorUI.isYQuadrantVisible()) {
                        setupYQuadrant()
                    }
                }
                start()
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = ScanDirection.DOWN

                    resetQuadrants()

                    if (!cursorUI.isYCursorLineVisible()) {
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
        CursorPoint.instance.point = point

        // check if drag is enabled, if so, select the end point
        if (GestureManager.getInstance().isDragging()) {
            GestureManager.getInstance().selectEndOfDrag()
            internalReset()
            return
        }

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
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
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
        }, delay)
    }

}

data class QuadrantInfo(
    val quadrantIndex: Int,
    val start: Int,
    val end: Int,
)

val MIN_QUADRANT_INDEX = 0
val MAX_QUADRANT_INDEX = 3