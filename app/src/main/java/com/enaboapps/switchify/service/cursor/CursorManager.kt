package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanReceiver
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import java.util.Timer
import java.util.TimerTask

/**
 * This class manages the cursor
 * @param context The context
 */
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


    /**
     * This function sets up the cursor
     */
    fun setup() {
        SwitchifyAccessibilityWindow.instance.setup(context)
        SwitchifyAccessibilityWindow.instance.show()

        CursorPoint.listener = this
    }


    /**
     * This function is called when the cursor point is reselected
     * It sets the quadrant to the last x quadrant
     */
    override fun onCursorPointReselect() {
        CursorPoint.y = 0
        // find the last quadrant
        quadrantInfo = CursorPoint.lastXQuadrant
        CursorPoint.x = quadrantInfo?.start!!
        isInQuadrant = true
        setupXCursorLine()
        startAutoScanIfEnabled()
    }


    /**
     * This function sets the quadrant info
     * @param quadrantIndex The quadrant index
     * @param start The start point of the quadrant
     * @param end The end point of the quadrant
     */
    private fun setQuadrantInfo(quadrantIndex: Int, start: Int, end: Int) {
        quadrantInfo = QuadrantInfo(quadrantIndex, start, end)

        when (direction) {
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                CursorPoint.lastXQuadrant = quadrantInfo!!
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                CursorPoint.lastYQuadrant = quadrantInfo!!
            }
        }
    }


    /**
     * This function sets up the y quadrant
     */
    private fun setupYQuadrant() {
        CursorPoint.y = CursorBounds.yMin(context)
        cursorUI.createYQuadrant(0)
        setQuadrantInfo(
            0,
            CursorPoint.y,
            CursorPoint.y + cursorUI.getQuadrantHeight()
        )
    }

    /**
     * This function updates the y quadrant to the given quadrant index
     */
    private fun updateYQuadrant(quadrantIndex: Int) {
        CursorPoint.y =
            CursorBounds.yMin(context) + (quadrantIndex * cursorUI.getQuadrantHeight())
        cursorUI.updateYQuadrant(quadrantIndex)
        setQuadrantInfo(
            quadrantIndex,
            CursorPoint.y,
            CursorPoint.y + cursorUI.getQuadrantHeight()
        )
    }

    /**
     * This function sets up the x quadrant
     */
    private fun setupXQuadrant() {
        CursorPoint.x = CursorBounds.X_MIN
        cursorUI.createXQuadrant(0)
        setQuadrantInfo(
            0,
            CursorPoint.x,
            CursorPoint.x + cursorUI.getQuadrantWidth()
        )
    }

    /**
     * This function updates the x quadrant to the given quadrant index
     */
    private fun updateXQuadrant(quadrantIndex: Int) {
        CursorPoint.x = quadrantIndex * cursorUI.getQuadrantWidth()
        cursorUI.updateXQuadrant(quadrantIndex)
        setQuadrantInfo(
            quadrantIndex,
            CursorPoint.x,
            CursorPoint.x + cursorUI.getQuadrantWidth()
        )
    }


    /**
     * This function sets up the y cursor line
     */
    private fun setupYCursorLine() {
        quadrantInfo?.quadrantIndex?.let {
            cursorUI.createYCursorLine(it)
        }
    }

    /**
     * This function updates the y cursor line to the current y position
     */
    private fun updateYCursorLine() {
        cursorUI.updateYCursorLine(CursorPoint.y)
    }

    /**
     * This function sets up the x cursor line
     */
    private fun setupXCursorLine() {
        quadrantInfo?.quadrantIndex?.let {
            cursorUI.createXCursorLine(it)
        }
    }

    /**
     * This function updates the x cursor line to the current x position
     */
    private fun updateXCursorLine() {
        cursorUI.updateXCursorLine(CursorPoint.x)
    }


    /**
     * This function starts the auto scan if it is enabled
     */
    private fun startAutoScanIfEnabled() {
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


    /**
     * This function swaps the scanning direction
     */
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


    /**
     * This function stops the scanning
     */
    override fun stopScanning() {
        scanningScheduler.stopScanning()
    }


    /**
     * This function pauses the scanning
     */
    override fun pauseScanning() {
        scanningScheduler.pauseScanning()
    }


    /**
     * This function resumes the scanning
     */
    override fun resumeScanning() {
        scanningScheduler.resumeScanning()
    }


    /**
     * This function moves the cursor
     */
    private fun move() {
        if (isInQuadrant) {
            moveCursorLine()
        } else {
            moveToNextQuadrant()
        }
    }


    /**
     * This function moves the cursor to the next quadrant
     */
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


    /**
     * This function moves the cursor line
     */
    private fun moveCursorLine() {
        if (quadrantInfo != null) {
            when (direction) {
                ScanDirection.LEFT ->
                    if (CursorPoint.x > quadrantInfo?.start!!) {
                        CursorPoint.x -= cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.RIGHT
                        moveCursorLine()
                    }

                ScanDirection.RIGHT ->
                    if (CursorPoint.x < quadrantInfo?.end!!) {
                        CursorPoint.x += cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.LEFT
                        moveCursorLine()
                    }

                ScanDirection.UP ->
                    if (CursorPoint.y > quadrantInfo?.start!!) {
                        CursorPoint.y -= cursorLineMovement
                        updateYCursorLine()
                    } else {
                        direction = ScanDirection.DOWN
                        moveCursorLine()
                    }

                ScanDirection.DOWN ->
                    if (CursorPoint.y < quadrantInfo?.end!!) {
                        CursorPoint.y += cursorLineMovement
                        updateYCursorLine()
                    } else {
                        direction = ScanDirection.UP
                        moveCursorLine()
                    }
            }
        }
    }


    /**
     * This function resets the cursor
     * It is used when the cursor is reset from outside the class
     */
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


    /**
     * This function resets the cursor
     */
    private fun internalReset() {
        stopScanning()

        direction = ScanDirection.RIGHT

        cursorUI.reset()
    }

    /**
     * This function checks if the cursor is reset
     */
    private fun isReset(): Boolean {
        return cursorUI.isReset()
    }

    /**
     * This function resets the quadrants
     */
    private fun resetQuadrants() {
        cursorUI.removeXQuadrant()
        cursorUI.removeYQuadrant()
    }


    /**
     * This function moves the cursor to the next item
     */
    fun moveToNextItem() {
        if (isReset()) {
            setupXQuadrant()
            startAutoScanIfEnabled()
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


    /**
     * This function moves the cursor to the previous item
     */
    fun moveToPreviousItem() {
        if (isReset()) {
            setupXQuadrant()
            startAutoScanIfEnabled()
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


    /**
     * This function performs the selection action
     */
    fun performSelectionAction() {
        stopScanning()

        // If the event is triggered within the auto select delay, we don't perform the action
        if (checkAutoSelectDelay()) {
            return
        }

        if (isReset()) {
            setupXQuadrant()
            startAutoScanIfEnabled()
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
                startAutoScanIfEnabled()
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                if (!isInQuadrant) {
                    isInQuadrant = true

                    direction = ScanDirection.DOWN

                    resetQuadrants()

                    if (!cursorUI.isYCursorLineVisible()) {
                        setupYCursorLine()
                    }

                    startAutoScanIfEnabled()
                } else {
                    isInQuadrant = false

                    performFinalAction()
                }
            }
        }
    }


    /**
     * This function performs the final action
     */
    private fun performFinalAction() {
        // check if drag is enabled, if so, select the end point
        if (GestureManager.getInstance().isDragging()) {
            GestureManager.getInstance().selectEndOfDrag()
            internalReset()
            return
        }

        // set the state from which the menu was activated
        MenuManager.getInstance().scanReceiverState = ScanReceiver.ReceiverState.CURSOR

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


    /**
     * This function checks if the auto select delay is triggered
     */
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


    /**
     * This function starts the auto select timer
     */
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