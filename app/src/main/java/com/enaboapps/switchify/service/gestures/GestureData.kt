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
            var swipeDirection: SwipeDirection? = null
            when (multiPointGestureType) {
                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_UP -> {
                    swipeDirection = SwipeDirection.UP
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_DOWN -> {
                    swipeDirection = SwipeDirection.DOWN
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_LEFT -> {
                    swipeDirection = SwipeDirection.LEFT
                }

                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_RIGHT -> {
                    swipeDirection = SwipeDirection.RIGHT
                }

                else -> {
                }
            }
            return GestureData(
                gestureType = typeFromMultiPointGestureType(multiPointGestureType),
                startPoint = startPoint,
                endPoint = endPoint,
                swipeDirection = swipeDirection
            )
        }

        fun typeFromMultiPointGestureType(
            multiPointGestureType: MultiPointGesturePerformer.MultiPointGestureType
        ): GestureType {
            return when (multiPointGestureType) {
                MultiPointGesturePerformer.MultiPointGestureType.DRAG -> GestureType.DRAG
                MultiPointGesturePerformer.MultiPointGestureType.CUSTOM_SWIPE -> GestureType.CUSTOM_SWIPE
                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_UP -> GestureType.SWIPE
                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_DOWN -> GestureType.SWIPE
                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_LEFT -> GestureType.SWIPE
                MultiPointGesturePerformer.MultiPointGestureType.SWIPE_RIGHT -> GestureType.SWIPE
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