package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import java.util.Timer
import java.util.TimerTask

class CursorManager(private val context: Context) : ScanStateInterface, CursorPointListener {

    companion object {
        private const val TAG = "CursorManager"

        private const val MIN_QUADRANT_INDEX = 0
    }

    private val cursorLineMovement = 40

    private val scanSettings = ScanSettings(context)

    private val uiHandler = Handler(Looper.getMainLooper())
    private val cursorUI = CursorUI(context, uiHandler)

    private var isInQuadrant = false
    private var quadrantInfo: QuadrantInfo? = null

    private var direction: ScanDirection = ScanDirection.RIGHT

    private val scanningScheduler = ScanningScheduler(context) { move() }

    // auto select variables
    private var isInAutoSelect = false // If true, we listen for a second event to activate the menu
    private var autoSelectTimer: Timer? = null // Timer to wait for the second event


    fun setup() {
        SwitchifyAccessibilityWindow.instance.setup(context)
        SwitchifyAccessibilityWindow.instance.show()

        CursorPoint.instance.listener = this
    }


    override fun onCursorPointReselect() {
        CursorPoint.instance.y = 0
        // find the last quadrant
        quadrantInfo = CursorPoint.instance.lastXQuadrant
        CursorPoint.instance.x = quadrantInfo?.start!!
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
        CursorPoint.instance.y = CursorBounds.yMin(context)
        cursorUI.createYQuadrant(0)
        setQuadrantInfo(
            0,
            CursorPoint.instance.y,
            CursorPoint.instance.y + cursorUI.getQuadrantHeight()
        )
    }

    private fun updateYQuadrant(quadrantIndex: Int) {
        CursorPoint.instance.y =
            CursorBounds.yMin(context) + (quadrantIndex * cursorUI.getQuadrantHeight())
        cursorUI.updateYQuadrant(quadrantIndex)
        setQuadrantInfo(
            quadrantIndex,
            CursorPoint.instance.y,
            CursorPoint.instance.y + cursorUI.getQuadrantHeight()
        )
    }

    private fun setupXQuadrant() {
        CursorPoint.instance.x = CursorBounds.X_MIN
        cursorUI.createXQuadrant(0)
        setQuadrantInfo(
            0,
            CursorPoint.instance.x,
            CursorPoint.instance.x + cursorUI.getQuadrantWidth()
        )
    }

    private fun updateXQuadrant(quadrantIndex: Int) {
        CursorPoint.instance.x = quadrantIndex * cursorUI.getQuadrantWidth()
        cursorUI.updateXQuadrant(quadrantIndex)
        setQuadrantInfo(
            quadrantIndex,
            CursorPoint.instance.x,
            CursorPoint.instance.x + cursorUI.getQuadrantWidth()
        )
    }


    private fun setupYCursorLine() {
        quadrantInfo?.quadrantIndex?.let {
            cursorUI.createYCursorLine(it)
        }
    }

    private fun updateYCursorLine() {
        cursorUI.updateYCursorLine(CursorPoint.instance.y)
    }

    private fun setupXCursorLine() {
        quadrantInfo?.quadrantIndex?.let {
            cursorUI.createXCursorLine(it)
        }
    }

    private fun updateXCursorLine() {
        cursorUI.updateXCursorLine(CursorPoint.instance.x)
    }


    private fun start() {
        if (scanSettings.isAutoScanMode()) {
            val rate = if (isInQuadrant) {
                scanSettings.getRefineScanRate()
            } else {
                scanSettings.getScanRate()
            }
            scanningScheduler.startScanning(rate, rate)
        }
    }


    /**
     * This function determines the max quadrant index
     * It uses the direction to determine the max quadrant index
     * The smaller the width or height of the screen, the smaller the max quadrant index
     * @return The max quadrant index
     */
    private fun getMaxQuadrantIndex(): Int {
        return when (direction) {
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                CursorUI.getNumberOfQuadrantsHorizontally(context) - 1
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                CursorUI.getNumberOfQuadrantsVertically(context) - 1
            }
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
                            updateXQuadrant(getMaxQuadrantIndex())
                        } else if (it.quadrantIndex == getMaxQuadrantIndex()) {
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
                            updateXQuadrant(getMaxQuadrantIndex())
                        } else if (it.quadrantIndex == getMaxQuadrantIndex()) {
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
                            updateYQuadrant(getMaxQuadrantIndex())
                        } else if (it.quadrantIndex == getMaxQuadrantIndex()) {
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
                            updateYQuadrant(getMaxQuadrantIndex())
                        } else if (it.quadrantIndex == getMaxQuadrantIndex()) {
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
                    if (it.quadrantIndex < getMaxQuadrantIndex()) {
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
                    if (it.quadrantIndex < getMaxQuadrantIndex()) {
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
                    if (CursorPoint.instance.x > quadrantInfo?.start!!) {
                        CursorPoint.instance.x -= cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.RIGHT
                        moveCursorLine()
                    }

                ScanDirection.RIGHT ->
                    if (CursorPoint.instance.x < quadrantInfo?.end!!) {
                        CursorPoint.instance.x += cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.LEFT
                        moveCursorLine()
                    }

                ScanDirection.UP ->
                    if (CursorPoint.instance.y > quadrantInfo?.start!!) {
                        CursorPoint.instance.y -= cursorLineMovement
                        updateYCursorLine()
                    } else {
                        direction = ScanDirection.DOWN
                        moveCursorLine()
                    }

                ScanDirection.DOWN ->
                    if (CursorPoint.instance.y < quadrantInfo?.end!!) {
                        CursorPoint.instance.y += cursorLineMovement
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
        // check if drag is enabled, if so, select the end point
        if (GestureManager.getInstance().isDragging()) {
            GestureManager.getInstance().selectEndOfDrag()
            internalReset()
            return
        }

        // check if auto select is enabled, if so, start the timer
        val auto = scanSettings.isAutoSelectEnabled()
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
            Log.d(Companion.TAG, "checkAutoSelectDelay: true")
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
        val delay = scanSettings.getAutoSelectDelay()
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