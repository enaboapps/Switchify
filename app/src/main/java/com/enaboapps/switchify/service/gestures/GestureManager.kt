package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.PointF
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorPoint
import com.enaboapps.switchify.service.gestures.visuals.GestureDrawing
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

        // This is the duration of the tap
        const val TAP_DURATION = 100L

        // This is the interval between the two taps
        const val DOUBLE_TAP_INTERVAL = 250L

        // This is the tap and hold duration
        const val TAP_AND_HOLD_DURATION = 1000L

        // This is the duration of the swipe
        const val SWIPE_DURATION = 80L

        // This is the duration of the drag
        const val DRAG_DURATION = 1500L

        // This is the duration of the zoom
        const val ZOOM_DURATION = 2000L

        // This is the amount of zoom
        const val ZOOM_AMOUNT = 300f
    }


    // Drag variables
    private var dragStartPoint: PointF? = null
    private var isDragging = false


    // swipe lock manager
    private var swipeLockManager: SwipeLockManager? = null


    // accessibility service
    private var accessibilityService: SwitchifyAccessibilityService? = null


    fun setup(accessibilityService: SwitchifyAccessibilityService) {
        this.accessibilityService = accessibilityService
        swipeLockManager = SwipeLockManager()
    }


    // Function to check if point is close to the center of the screen (within 400 pixels)
    fun isPointCloseToCenter(): Boolean {
        val point = CursorPoint.instance.point ?: return false
        accessibilityService?.let {
            val width = ScreenUtils.getWidth(it)
            val height = ScreenUtils.getHeight(it)
            val centerX = width / 2
            val centerY = height / 2
            val distance = sqrt(
                (point.x - centerX).toDouble().pow(2.0) + (point.y - centerY).toDouble().pow(2.0)
            )
            return distance <= 400
        }
        return false
    }


    // Function to perform a tap
    fun performTap() {
        try {
            accessibilityService.let {
                val path = android.graphics.Path()
                val currentPoint = CursorPoint.instance.point
                currentPoint?.let { point ->
                    val gestureDrawing = GestureDrawing(it!!)
                    gestureDrawing.drawCircleAndRemove(
                        point.x.toInt(),
                        point.y.toInt(),
                        TAP_DURATION
                    )
                    path.moveTo(point.x, point.y)
                }
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_DURATION)).build()
                it?.dispatchGesture(
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
                val currentPoint = CursorPoint.instance.point
                currentPoint?.let { point ->
                    val gestureDrawing = GestureDrawing(it!!)
                    gestureDrawing.drawCircleAndRemove(
                        point.x.toInt(),
                        point.y.toInt(),
                        TAP_DURATION
                    )
                    path.moveTo(point.x, point.y)
                }
                val tap1 = GestureDescription.StrokeDescription(path, 0, TAP_DURATION)
                val tap2 =
                    GestureDescription.StrokeDescription(path, DOUBLE_TAP_INTERVAL, TAP_DURATION)
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(tap1)
                    .addStroke(tap2)
                    .build()
                it?.dispatchGesture(
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
                val currentPoint = CursorPoint.instance.point
                currentPoint?.let { point ->
                    val gestureDrawing = GestureDrawing(it!!)
                    gestureDrawing.drawCircleAndRemove(
                        point.x.toInt(),
                        point.y.toInt(),
                        TAP_AND_HOLD_DURATION
                    )
                    path.moveTo(point.x, point.y)
                }
                val gestureDescription = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_AND_HOLD_DURATION))
                    .build()
                it?.dispatchGesture(
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

    // Swipe direction
    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }

    // Function to lock/unlock the swipe lock
    fun toggleSwipeLock() {
        swipeLockManager?.toggleSwipeLock()
    }

    // Function to check if the swipe lock enabled
    fun isSwipeLockEnabled(): Boolean {
        return swipeLockManager?.isSwipeLockEnabled() ?: false
    }

    // Function to perform the swipe lock, if it's locked
    // Returns true if the swipe lock is locked, false otherwise
    fun performSwipeLock(): Boolean {
        if (isSwipeLockEnabled()) {
            performSwipe(swipeLockManager?.swipeLockDirection ?: SwipeDirection.UP)
            return true
        }
        return false
    }

    // Function to perform a swipe
    fun performSwipe(direction: SwipeDirection) {
        try {
            if (swipeLockManager?.isLocked == true) {
                swipeLockManager?.swipeLockDirection = direction
            }
            val path = android.graphics.Path()
            val currentPoint = CursorPoint.instance.point
            currentPoint?.let { point ->
                path.moveTo(point.x, point.y)
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
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onSwipe: ", e)
        }
    }

    // Function to start dragging
    fun startDragGesture() {
        dragStartPoint = CursorPoint.instance.point
        isDragging = true

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
        val currentPoint = CursorPoint.instance.point
        currentPoint?.let { point ->
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
        }

        // Reset the drag start point
        dragStartPoint = null
    }

    fun isDragging(): Boolean {
        return isDragging
    }

    enum class ZoomAction {
        ZOOM_IN, ZOOM_OUT
    }

    // Function to zoom in
    fun performZoomAction(zoomAction: ZoomAction) {
        val centerPoint =
            CursorPoint.instance.point // Assuming this is a singleton with a current point
        if (centerPoint != null) {
            // Initialize paths for the two fingers
            val path1 = android.graphics.Path()
            val path2 = android.graphics.Path()

            // Figure out the zoom point
            val leftZoomPoint = getInBoundsCoordinate(
                ScreenUtils.getWidth(accessibilityService!!),
                centerPoint.x - ZOOM_AMOUNT
            )
            val rightZoomPoint = getInBoundsCoordinate(
                ScreenUtils.getWidth(accessibilityService!!),
                centerPoint.x + ZOOM_AMOUNT
            )

            when (zoomAction) {
                ZoomAction.ZOOM_IN -> {
                    // Setup paths for zooming in (fingers moving apart)
                    path1.moveTo(centerPoint.x, centerPoint.y)
                    path1.lineTo(leftZoomPoint, centerPoint.y)

                    path2.moveTo(centerPoint.x, centerPoint.y)
                    path2.lineTo(rightZoomPoint, centerPoint.y)
                }

                ZoomAction.ZOOM_OUT -> {
                    // Setup paths for zooming out (fingers moving together)
                    path1.moveTo(leftZoomPoint, centerPoint.y)
                    path1.lineTo(centerPoint.x, centerPoint.y)

                    path2.moveTo(rightZoomPoint, centerPoint.y)
                    path2.lineTo(centerPoint.x, centerPoint.y)
                }
            }

            // Create gesture description with the paths
            val gestureBuilder = GestureDescription.Builder()
            val stroke1 = GestureDescription.StrokeDescription(path1, 0, ZOOM_DURATION)
            val stroke2 = GestureDescription.StrokeDescription(path2, 0, ZOOM_DURATION)
            gestureBuilder.addStroke(stroke1).addStroke(stroke2)

            val gestureDescription = gestureBuilder.build()

            // Dispatch the gesture
            try {
                accessibilityService?.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            // Log.d(TAG, "onCompleted")
                        }
                    },
                    null
                )
            } catch (e: Exception) {
                // Log.e(TAG, "onZoom: ", e)
            }
        } else {
            // Handle the case where centerPoint is null
        }
    }

    // Helper function to figure out if the gesture is going to be out of bounds
    // Takes in two floats, the width or height of the screen, and the target coordinate
    // Returns the target coordinate if it's within bounds, or the closest bound if it's out of bounds
    private fun getInBoundsCoordinate(widthOrHeight: Int, target: Float): Float {
        return if (target <= 0) {
            0f
        } else if (target >= widthOrHeight) {
            widthOrHeight.toFloat()
        } else {
            target
        }
    }

}