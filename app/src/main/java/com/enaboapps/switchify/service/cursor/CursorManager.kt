package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.gestures.GesturePointListener
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.selection.AutoSelectionHandler

/**
 * This class manages the cursor
 * @param context The context
 */
class CursorManager(private val context: Context) : ScanStateInterface, GesturePointListener {

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

    private var scanningScheduler: ScanningScheduler? = null

    /**
     * This function sets up the cursor
     */
    private fun setup() {
        if (isSetupRequired()) {
            GesturePoint.listener = this
            scanningScheduler = ScanningScheduler(context) { move() }
        }

        CursorMode.init(context)
    }

    /**
     * This function checks if setup is required
     * @return True if setup is required, false otherwise
     */
    private fun isSetupRequired(): Boolean {
        return scanningScheduler == null
    }


    /**
     * This function is called when the cursor point is reselected
     * It sets the quadrant to the last x quadrant
     */
    override fun onGesturePointReselect() {
        GesturePoint.y = 0
        // find the last quadrant
        quadrantInfo = GesturePoint.lastXQuadrant
        GesturePoint.x = quadrantInfo?.start!!
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
                GesturePoint.lastXQuadrant = quadrantInfo!!
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                GesturePoint.lastYQuadrant = quadrantInfo!!
            }
        }
    }


    /**
     * This function sets up the y quadrant
     */
    private fun setupYQuadrant() {
        GesturePoint.y = CursorBounds.Y_MIN
        cursorUI.createYQuadrant(0)
        setQuadrantInfo(
            0,
            GesturePoint.y,
            GesturePoint.y + cursorUI.getQuadrantHeight()
        )
    }

    /**
     * This function updates the y quadrant to the given quadrant index
     */
    private fun updateYQuadrant(quadrantIndex: Int) {
        GesturePoint.y =
            CursorBounds.Y_MIN + (quadrantIndex * cursorUI.getQuadrantHeight())
        cursorUI.updateYQuadrant(quadrantIndex)
        setQuadrantInfo(
            quadrantIndex,
            GesturePoint.y,
            GesturePoint.y + cursorUI.getQuadrantHeight()
        )
    }

    /**
     * This function sets up the x quadrant
     */
    private fun setupXQuadrant() {
        GesturePoint.x = CursorBounds.X_MIN
        cursorUI.createXQuadrant(0)
        setQuadrantInfo(
            0,
            GesturePoint.x,
            GesturePoint.x + cursorUI.getQuadrantWidth()
        )
    }

    /**
     * This function updates the x quadrant to the given quadrant index
     */
    private fun updateXQuadrant(quadrantIndex: Int) {
        GesturePoint.x = quadrantIndex * cursorUI.getQuadrantWidth()
        cursorUI.updateXQuadrant(quadrantIndex)
        setQuadrantInfo(
            quadrantIndex,
            GesturePoint.x,
            GesturePoint.x + cursorUI.getQuadrantWidth()
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
        // Account for the cursor line thickness
        val y = GesturePoint.y
        if (y < quadrantInfo?.start!!) {
            GesturePoint.y = quadrantInfo?.start!!
        } else if (y >= quadrantInfo?.end!!) {
            GesturePoint.y = quadrantInfo?.end!! - CursorUI.CURSOR_LINE_THICKNESS
        }

        cursorUI.updateYCursorLine(GesturePoint.y)
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
        // Account for the cursor line thickness
        val x = GesturePoint.x
        if (x < quadrantInfo?.start!!) {
            GesturePoint.x = quadrantInfo?.start!!
        } else if (x >= quadrantInfo?.end!!) {
            GesturePoint.x = quadrantInfo?.end!! - CursorUI.CURSOR_LINE_THICKNESS
        }
        
        cursorUI.updateXCursorLine(GesturePoint.x)
    }


    /**
     * This function starts the auto scan if it is enabled
     */
    private fun startAutoScanIfEnabled() {
        if (scanSettings.isAutoScanMode()) {
            val rate = if (isInQuadrant || CursorMode.isSingleMode()) {
                scanSettings.getRefineScanRate()
            } else {
                scanSettings.getScanRate()
            }
            scanningScheduler?.startScanning(rate, rate)
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
                CursorUI.getNumberOfQuadrants(CursorBounds.width(context)) - 1
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                CursorUI.getNumberOfQuadrants(CursorBounds.height(context)) - 1
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
        scanningScheduler?.stopScanning()
    }


    /**
     * This function pauses the scanning
     */
    override fun pauseScanning() {
        scanningScheduler?.pauseScanning()
    }


    /**
     * This function resumes the scanning
     */
    override fun resumeScanning() {
        scanningScheduler?.resumeScanning()
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
                    if (GesturePoint.x > (quadrantInfo?.start!! + CursorUI.CURSOR_LINE_THICKNESS)) {
                        GesturePoint.x -= cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.RIGHT
                        moveCursorLine()
                    }

                ScanDirection.RIGHT ->
                    if (GesturePoint.x < (quadrantInfo?.end!! - CursorUI.CURSOR_LINE_THICKNESS)) {
                        GesturePoint.x += cursorLineMovement
                        updateXCursorLine()
                    } else {
                        direction = ScanDirection.LEFT
                        moveCursorLine()
                    }

                ScanDirection.UP ->
                    if (GesturePoint.y > (quadrantInfo?.start!! + CursorUI.CURSOR_LINE_THICKNESS)) {
                        GesturePoint.y -= cursorLineMovement
                        updateYCursorLine()
                    } else {
                        direction = ScanDirection.DOWN
                        moveCursorLine()
                    }

                ScanDirection.DOWN ->
                    if (GesturePoint.y < (quadrantInfo?.end!! - CursorUI.CURSOR_LINE_THICKNESS)) {
                        GesturePoint.y += cursorLineMovement
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
        setup()

        stopScanning()

        if (isReset()) {
            if (CursorMode.isBlockMode()) {
                setupXQuadrant()
            } else {
                isInQuadrant = true
                setQuadrantInfo(0, CursorBounds.X_MIN, CursorBounds.width(context))
                GesturePoint.x = CursorBounds.X_MIN
                setupXCursorLine()
            }
            startAutoScanIfEnabled()
            return
        }

        // We perform the action based on the direction
        when (direction) {
            ScanDirection.LEFT, ScanDirection.RIGHT -> {
                if (!isInQuadrant && CursorMode.isBlockMode()) {
                    isInQuadrant = true

                    direction = ScanDirection.RIGHT

                    resetQuadrants()

                    if (!cursorUI.isXCursorLineVisible()) {
                        setupXCursorLine()
                    }
                } else {
                    direction = ScanDirection.DOWN
                    if (CursorMode.isBlockMode()) {
                        isInQuadrant = false

                        if (!cursorUI.isYQuadrantVisible()) {
                            setupYQuadrant()
                        }
                    } else {
                        setQuadrantInfo(0, CursorBounds.Y_MIN, CursorBounds.height(context))
                        GesturePoint.y = CursorBounds.Y_MIN

                        if (!cursorUI.isYCursorLineVisible()) {
                            setupYCursorLine()
                        }
                    }
                }
                startAutoScanIfEnabled()
            }

            ScanDirection.UP, ScanDirection.DOWN -> {
                if (!isInQuadrant && CursorMode.isBlockMode()) {
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

        AutoSelectionHandler.setSelectAction { performTapAction() }
        AutoSelectionHandler.performSelectionAction()

        internalReset()
    }


    /**
     * This function performs the tap action
     */
    private fun performTapAction() {
        GestureManager.getInstance().performTap()
    }

    /**
     * This function cleans up the cursor
     */
    fun cleanup() {
        cursorUI.reset()
        scanningScheduler?.shutdown()
        scanningScheduler = null
    }
}