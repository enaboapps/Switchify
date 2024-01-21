package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.PointF
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.utils.ScreenUtils
import java.util.Timer

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


    // Variable to keep track of the current point
    var currentPoint: PointF? = null


    // swipe lock manager
    private var swipeLockManager: SwipeLockManager? = null


    // accessibility service
    private var accessibilityService: SwitchifyAccessibilityService? = null


    fun setup(accessibilityService: SwitchifyAccessibilityService) {
        this.accessibilityService = accessibilityService
        swipeLockManager = SwipeLockManager(accessibilityService)
    }

    // Function to perform a tap
    fun performTap() {
        try {
            val path = android.graphics.Path()
            currentPoint?.let { point ->
                path.moveTo(point.x, point.y)
            }
            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 550, 100)).build()
            accessibilityService.let {
                it?.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            val gestureDrawing = GestureDrawing(it)
                            currentPoint?.let { point ->
                                gestureDrawing.drawCircleAndRemove(point.x.toInt(), point.y.toInt())
                            }
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
            val path = android.graphics.Path()
            currentPoint?.let { point ->
                path.moveTo(point.x, point.y)
            }
            val gestureDescription = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 550, 100)).build()
            accessibilityService.let {
                it?.dispatchGesture(
                    gestureDescription,
                    object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription?) {
                            super.onCompleted(gestureDescription)
                            val gestureDrawing = GestureDrawing(it)
                            currentPoint?.let { point ->
                                gestureDrawing.drawCircleAndRemove(point.x.toInt(), point.y.toInt())
                            }
                            Timer().schedule(object : java.util.TimerTask() {
                                override fun run() {
                                    if (gestureDescription != null) {
                                        it.dispatchGesture(
                                            gestureDescription,
                                            object : AccessibilityService.GestureResultCallback() {
                                                override fun onCompleted(gestureDescription: GestureDescription?) {
                                                    super.onCompleted(gestureDescription)
                                                    currentPoint?.let { point ->
                                                        gestureDrawing.drawCircleAndRemove(
                                                            point.x.toInt(),
                                                            point.y.toInt()
                                                        )
                                                    }
                                                }
                                            },
                                            null
                                        )
                                    }
                                }
                            }, 100)
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onDoubleTap: ", e)
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
            currentPoint?.let { point ->
                path.moveTo(point.x, point.y)
                accessibilityService?.let { accessibilityService ->
                    val gestureDrawing = GestureDrawing(accessibilityService)
                    when (direction) {
                        SwipeDirection.UP -> {
                            val fifthOfScreen = ScreenUtils.getHeight(accessibilityService) / 5
                            val travel = getTravel(
                                ScreenUtils.getHeight(accessibilityService),
                                point.y - fifthOfScreen
                            )
                            path.lineTo(point.x, travel)
                            gestureDrawing.drawLineAndRemove(
                                point.x.toInt(),
                                point.y.toInt(),
                                point.x.toInt(),
                                travel.toInt()
                            )
                        }

                        SwipeDirection.DOWN -> {
                            val fifthOfScreen = ScreenUtils.getHeight(accessibilityService) / 5
                            val travel = getTravel(
                                ScreenUtils.getHeight(accessibilityService),
                                point.y + fifthOfScreen
                            )
                            path.lineTo(point.x, travel)
                            gestureDrawing.drawLineAndRemove(
                                point.x.toInt(),
                                point.y.toInt(),
                                point.x.toInt(),
                                travel.toInt()
                            )
                        }

                        SwipeDirection.LEFT -> {
                            val quarterOfScreen = ScreenUtils.getWidth(accessibilityService) / 4
                            val travel = getTravel(
                                ScreenUtils.getWidth(accessibilityService),
                                point.x - quarterOfScreen
                            )
                            path.lineTo(travel, point.y)
                            gestureDrawing.drawLineAndRemove(
                                point.x.toInt(),
                                point.y.toInt(),
                                travel.toInt(),
                                point.y.toInt()
                            )
                        }

                        SwipeDirection.RIGHT -> {
                            val quarterOfScreen = ScreenUtils.getWidth(accessibilityService) / 4
                            val travel = getTravel(
                                ScreenUtils.getWidth(accessibilityService),
                                point.x + quarterOfScreen
                            )
                            path.lineTo(travel, point.y)
                            gestureDrawing.drawLineAndRemove(
                                point.x.toInt(),
                                point.y.toInt(),
                                travel.toInt(),
                                point.y.toInt()
                            )
                        }
                    }
                    val gestureDescription = GestureDescription.Builder()
                        .addStroke(GestureDescription.StrokeDescription(path, 550, 100)).build()
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

    // Helper function to figure out if the gesture is going to be out of bounds
    // Takes in two floats, the width or height of the screen, and the travel distance
    // Returns the travel if it's <= or >= 0 otherwise returns the width or height, or 0
    private fun getTravel(widthOrHeight: Int, travel: Float): Float {
        return if (travel <= 0) {
            0f
        } else if (travel >= widthOrHeight) {
            widthOrHeight.toFloat()
        } else {
            travel
        }
    }

}