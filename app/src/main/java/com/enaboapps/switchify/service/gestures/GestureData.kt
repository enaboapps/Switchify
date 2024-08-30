package com.enaboapps.switchify.service.gestures

import android.graphics.PointF

data class GestureData(
    val gestureType: GestureType,
    val startPoint: PointF,
    val endPoint: PointF? = null,
    val swipeDirection: SwipeDirection? = null,
    val zoomAction: ZoomGesturePerformer.ZoomAction? = null
) {
    enum class GestureType {
        TAP,
        DOUBLE_TAP,
        TAP_AND_HOLD,
        SWIPE,
        DRAG,
        ZOOM
    }

    companion object {
        const val TAP_DURATION = 100L
        const val DOUBLE_TAP_INTERVAL = 250L
        const val TAP_AND_HOLD_DURATION = 1000L
        const val SWIPE_DURATION = 80L
        const val DRAG_DURATION = 1500L
    }

    fun performLockAction(gestureManager: GestureManager): Boolean {
        if (isLockAvailable()) {
            when (gestureType) {
                GestureType.TAP -> {
                    gestureManager.performTap()
                    return true
                }

                GestureType.DOUBLE_TAP -> {
                    gestureManager.performDoubleTap()
                    return true
                }

                GestureType.TAP_AND_HOLD -> {
                    gestureManager.performTapAndHold()
                    return true
                }

                GestureType.SWIPE -> {
                    swipeDirection?.let { direction ->
                        gestureManager.performSwipe(direction)
                        return true
                    }
                }

                GestureType.ZOOM -> {
                    zoomAction?.let { action ->
                        gestureManager.performZoomAction(action)
                        return true
                    }
                }

                else -> {
                    return false
                }
            }
        }
        return false
    }

    private fun isLockAvailable(): Boolean {
        return gestureType != GestureType.DRAG
    }
}

enum class SwipeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}