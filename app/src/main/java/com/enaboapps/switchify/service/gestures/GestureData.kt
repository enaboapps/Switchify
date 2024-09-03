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
        CUSTOM_SWIPE,
        DRAG,
        ZOOM
    }

    companion object {
        const val TAP_DURATION = 100L
        const val DOUBLE_TAP_INTERVAL = 250L
        const val TAP_AND_HOLD_DURATION = 1000L
        const val SWIPE_DURATION = 80L
        const val DRAG_DURATION = 1500L

        fun fromMultiPointGestureType(
            multiPointGestureType: MultiPointGesturePerformer.MultiPointGestureType,
            startPoint: PointF,
            endPoint: PointF? = null
        ): GestureData {
            return when (multiPointGestureType) {
                MultiPointGesturePerformer.MultiPointGestureType.DRAG -> {
                    GestureData(GestureType.DRAG, startPoint, endPoint)
                }

                MultiPointGesturePerformer.MultiPointGestureType.CUSTOM_SWIPE -> {
                    GestureData(GestureType.CUSTOM_SWIPE, startPoint, endPoint)
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_UP -> {
                    GestureData(GestureType.SWIPE, startPoint, endPoint, SwipeDirection.UP)
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_DOWN -> {
                    GestureData(GestureType.SWIPE, startPoint, endPoint, SwipeDirection.DOWN)
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_LEFT -> {
                    GestureData(GestureType.SWIPE, startPoint, endPoint, SwipeDirection.LEFT)
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_RIGHT -> {
                    GestureData(GestureType.SWIPE, startPoint, endPoint, SwipeDirection.RIGHT)
                }
            }
        }
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
        return gestureType != GestureType.DRAG && gestureType != GestureType.CUSTOM_SWIPE
    }
}

enum class SwipeDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}