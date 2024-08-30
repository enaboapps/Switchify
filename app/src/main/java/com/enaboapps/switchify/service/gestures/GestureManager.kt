package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.PointF
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureData.Companion.DOUBLE_TAP_INTERVAL
import com.enaboapps.switchify.service.gestures.GestureData.Companion.DRAG_DURATION
import com.enaboapps.switchify.service.gestures.GestureData.Companion.SWIPE_DURATION
import com.enaboapps.switchify.service.gestures.GestureData.Companion.TAP_AND_HOLD_DURATION
import com.enaboapps.switchify.service.gestures.GestureData.Companion.TAP_DURATION
import com.enaboapps.switchify.service.gestures.utils.GestureUtils.getInBoundsCoordinate
import com.enaboapps.switchify.service.gestures.visuals.GestureDrawing
import com.enaboapps.switchify.service.nodes.NodeExaminer
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.utils.ScreenUtils
import com.enaboapps.switchify.service.window.ServiceMessageHUD
import kotlin.math.pow
import kotlin.math.sqrt

class GestureManager {
    // singleton
    companion object {
        private var instance: GestureManager? = null
        fun getInstance(): GestureManager {
            if (instance == null) {
                instance = GestureManager()
            }
            return instance!!
        }
    }


    // Drag variables
    private var dragStartPoint: PointF? = null
    private var isDragging = false


    // gesture lock manager
    private var gestureLockManager: GestureLockManager? = null


    // accessibility service
    private var accessibilityService: SwitchifyAccessibilityService? = null


    // preference manager
    private var preferenceManager: PreferenceManager? = null


    fun setup(accessibilityService: SwitchifyAccessibilityService) {
        this.accessibilityService = accessibilityService
        gestureLockManager = GestureLockManager()
        preferenceManager = PreferenceManager(accessibilityService)
    }


    // Function to check if point is close to the center of the screen (within 400 pixels)
    fun isPointCloseToCenter(): Boolean {
        val point = GesturePoint.getPoint()
        accessibilityService?.let {
            val width = ScreenUtils.getWidth(it)
            val height = ScreenUtils.getHeight(it)
            val centerX = width / 2
            val centerY = height / 2
            val distance = sqrt(
                (point.x - centerX).toDouble().pow(2.0) + (point.y - centerY).toDouble().pow(2.0)
            )
            return distance <= ScreenUtils.dpToPx(it, 350)
        }
        return false
    }


    // Function to get current point
    private fun getAssistedCurrentPoint(): PointF {
        return if (preferenceManager?.getBooleanValue(PreferenceManager.PREFERENCE_KEY_ASSISTED_SELECTION) == true) {
            NodeExaminer.getClosestNodeToPoint(GesturePoint.getPoint())
        } else {
            GesturePoint.getPoint()
        }
    }


    // Function to perform a tap
    fun performTap() {
        try {
            accessibilityService.let {
                val path = android.graphics.Path()
                val point = getAssistedCurrentPoint()
                val gestureDrawing = GestureDrawing(it!!)
                gestureDrawing.drawCircleAndRemove(
                    point.x.toInt(),
                    point.y.toInt(),
                    TAP_DURATION
                )
                path.moveTo(point.x, point.y)
                gestureLockManager?.setLockedGestureData(
                    GestureData(
                        GestureData.GestureType.TAP,
                        point
                    )
                )
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_DURATION)).build()
                it.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onTap: ", e)
        }
    }

    // Function to perform a double tap
    fun performDoubleTap() {
        try {
            accessibilityService.let {
                val path = android.graphics.Path()
                val point = getAssistedCurrentPoint()
                val gestureDrawing = GestureDrawing(it!!)
                gestureDrawing.drawCircleAndRemove(
                    point.x.toInt(),
                    point.y.toInt(),
                    TAP_DURATION
                )
                path.moveTo(point.x, point.y)
                gestureLockManager?.setLockedGestureData(
                    GestureData(
                        GestureData.GestureType.DOUBLE_TAP,
                        point
                    )
                )
                val tap1 = GestureDescription.StrokeDescription(path, 0, TAP_DURATION)
                val tap2 =
                    GestureDescription.StrokeDescription(path, DOUBLE_TAP_INTERVAL, TAP_DURATION)
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(tap1)
                    .addStroke(tap2)
                    .build()
                it.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            // Log.d(TAG, "onCompleted")
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onDoubleTap: ", e)
        }
    }

    // Function to perform a tap and hold
    fun performTapAndHold() {
        try {
            accessibilityService.let {
                val path = android.graphics.Path()
                val point = getAssistedCurrentPoint()
                val gestureDrawing = GestureDrawing(it!!)
                gestureDrawing.drawCircleAndRemove(
                    point.x.toInt(),
                    point.y.toInt(),
                    TAP_AND_HOLD_DURATION
                )
                path.moveTo(point.x, point.y)
                gestureLockManager?.setLockedGestureData(
                    GestureData(
                        GestureData.GestureType.TAP_AND_HOLD,
                        point
                    )
                )
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_AND_HOLD_DURATION))
                    .build()
                it.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            // Log.d(TAG, "onCompleted")
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onTapAndHold: ", e)
        }
    }


    // Function to perform the gesture lock action if the gesture lock is enabled
    // Returns true if the gesture lock action was performed
    fun performGestureLockAction(): Boolean {
        if (gestureLockManager?.isGestureLockEnabled() == true) {
            gestureLockManager?.getLockedGestureData()?.let { gestureData ->
                if (gestureLockManager?.canLockGesture(gestureData.gestureType) == true) {
                    return gestureData.performLockAction(this)
                }
            }
        }
        return false
    }


    // Function to toggle the gesture lock
    fun toggleGestureLock() {
        gestureLockManager?.toggleGestureLock()
    }


    // Function to check if the gesture lock is enabled
    fun isGestureLockEnabled(): Boolean {
        return gestureLockManager?.isGestureLockEnabled() == true
    }


    // Function to perform a swipe
    fun performSwipe(direction: SwipeDirection) {
        try {
            val path = android.graphics.Path()
            val point = GesturePoint.getPoint()
            path.moveTo(point.x, point.y)
            gestureLockManager?.setLockedGestureData(
                GestureData(
                    GestureData.GestureType.SWIPE,
                    point,
                    swipeDirection = direction
                )
            )
            accessibilityService?.let { accessibilityService ->
                val gestureDrawing = GestureDrawing(accessibilityService)
                when (direction) {
                    SwipeDirection.UP -> {
                        val fifthOfScreen = ScreenUtils.getHeight(accessibilityService) / 5
                        val travel = getInBoundsCoordinate(
                            ScreenUtils.getHeight(accessibilityService),
                            point.y - fifthOfScreen
                        )
                        path.lineTo(point.x, travel)
                        gestureDrawing.drawLineAndArrowAndRemove(
                            point.x.toInt(),
                            point.y.toInt(),
                            point.x.toInt(),
                            travel.toInt(),
                            500
                        )
                    }

                    SwipeDirection.DOWN -> {
                        val fifthOfScreen = ScreenUtils.getHeight(accessibilityService) / 5
                        val travel = getInBoundsCoordinate(
                            ScreenUtils.getHeight(accessibilityService),
                            point.y + fifthOfScreen
                        )
                        path.lineTo(point.x, travel)
                        gestureDrawing.drawLineAndArrowAndRemove(
                            point.x.toInt(),
                            point.y.toInt(),
                            point.x.toInt(),
                            travel.toInt(),
                            500
                        )
                    }

                    SwipeDirection.LEFT -> {
                        val quarterOfScreen = ScreenUtils.getWidth(accessibilityService) / 4
                        val travel = getInBoundsCoordinate(
                            ScreenUtils.getWidth(accessibilityService),
                            point.x - quarterOfScreen
                        )
                        path.lineTo(travel, point.y)
                        gestureDrawing.drawLineAndArrowAndRemove(
                            point.x.toInt(),
                            point.y.toInt(),
                            travel.toInt(),
                            point.y.toInt(),
                            500
                        )
                    }

                    SwipeDirection.RIGHT -> {
                        val quarterOfScreen = ScreenUtils.getWidth(accessibilityService) / 4
                        val travel = getInBoundsCoordinate(
                            ScreenUtils.getWidth(accessibilityService),
                            point.x + quarterOfScreen
                        )
                        path.lineTo(travel, point.y)
                        gestureDrawing.drawLineAndArrowAndRemove(
                            point.x.toInt(),
                            point.y.toInt(),
                            travel.toInt(),
                            point.y.toInt(),
                            500
                        )
                    }
                }
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, SWIPE_DURATION))
                    .build()
                accessibilityService.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            // Log.d(TAG, "onCompleted")
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onSwipe: ", e)
        }
    }

    // Function to start dragging
    fun startDragGesture() {
        dragStartPoint = GesturePoint.getPoint()
        isDragging = true

        ScanMethod.setType(ScanMethod.MethodType.CURSOR)

        ServiceMessageHUD.instance.showMessage(
            "Select where to drag to",
            ServiceMessageHUD.MessageType.DISAPPEARING
        )
    }

    // Function to stop dragging
    fun selectEndOfDrag() {
        isDragging = false

        // If the drag start point is null, return
        if (dragStartPoint == null) {
            return
        }

        // Dispatch the drag gesture
        val path = android.graphics.Path()
        val point = GesturePoint.getPoint()
        path.moveTo(dragStartPoint!!.x, dragStartPoint!!.y)
        path.lineTo(point.x, point.y)
        accessibilityService?.let { accessibilityService ->
            val gestureDrawing = GestureDrawing(accessibilityService)
            gestureDrawing.drawLineAndArrowAndRemove(
                dragStartPoint!!.x.toInt(),
                dragStartPoint!!.y.toInt(),
                point.x.toInt(),
                point.y.toInt(),
                500
            )
            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, DRAG_DURATION)).build()
            accessibilityService.dispatchGesture(
                gestureDescription,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        // Log.d(TAG, "onCompleted")
                    }
                },
                null
            )
        }

        // Reset the drag start point
        dragStartPoint = null
    }

    fun isDragging(): Boolean {
        return isDragging
    }

    // Function to perform a zoom
    fun performZoomAction(zoomAction: ZoomGesturePerformer.ZoomAction) {
        gestureLockManager?.setLockedGestureData(
            GestureData(
                GestureData.GestureType.ZOOM,
                GesturePoint.getPoint(),
                zoomAction = zoomAction
            )
        )
        accessibilityService?.let {
            ZoomGesturePerformer.performZoomAction(zoomAction, it)
        }
    }

}