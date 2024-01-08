package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.PointF
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.utils.ScreenUtils

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

    // accessibility service
    var accessibilityService: SwitchifyAccessibilityService? = null

    // Function to perform a tap
    fun performTap() {
        try {
            val path = android.graphics.Path()
            currentPoint?.let { point ->
                path.moveTo(point.x, point.y)
            }
            val gestureDescription = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 550, 100)).build()
            accessibilityService.let {
                it?.dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        val gestureDrawing = GestureDrawing(it)
                        currentPoint?.let { point ->
                            gestureDrawing.drawCircleAndRemove(point.x.toInt(), point.y.toInt())
                        }
                    }
                }, null)
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onTap: ", e)
        }
    }

    // Swipe direction
    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }

    // Function to perform a swipe
    fun performSwipe(direction: SwipeDirection) {
        try {
            val path = android.graphics.Path()
            currentPoint?.let { point ->
                path.moveTo(point.x, point.y)
                accessibilityService?.let { accessibilityService ->
                    when (direction) {
                        SwipeDirection.UP -> {
                            val fifthOfScreen = ScreenUtils.getHeight(accessibilityService) / 5
                            val travel = getTravel(ScreenUtils.getHeight(accessibilityService), point.y - fifthOfScreen)
                            path.lineTo(point.x, travel)
                        }

                        SwipeDirection.DOWN -> {
                            val fifthOfScreen = ScreenUtils.getHeight(accessibilityService) / 5
                            val travel = getTravel(ScreenUtils.getHeight(accessibilityService), point.y + fifthOfScreen)
                            path.lineTo(point.x, travel)
                        }

                        SwipeDirection.LEFT -> {
                            val quarterOfScreen = ScreenUtils.getWidth(accessibilityService) / 4
                            val travel = getTravel(ScreenUtils.getWidth(accessibilityService), point.x - quarterOfScreen)
                            path.lineTo(travel, point.y)
                        }

                        SwipeDirection.RIGHT -> {
                            val quarterOfScreen = ScreenUtils.getWidth(accessibilityService) / 4
                            val travel = getTravel(ScreenUtils.getWidth(accessibilityService), point.x + quarterOfScreen)
                            path.lineTo(travel, point.y)
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