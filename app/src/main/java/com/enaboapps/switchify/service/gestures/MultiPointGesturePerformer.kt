package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureData.Companion.DRAG_DURATION
import com.enaboapps.switchify.service.gestures.GestureData.Companion.SWIPE_DURATION
import com.enaboapps.switchify.service.gestures.visuals.GestureDrawing
import com.enaboapps.switchify.service.utils.ScreenUtils
import com.enaboapps.switchify.service.window.ServiceMessageHUD

/**
 * Performs multi-point gestures such as drags and swipes.
 *
 * @property accessibilityService The accessibility service to use for performing gestures.
 * @property gestureLockManager The gesture lock manager to use for locking gestures.
 */
class MultiPointGesturePerformer(
    private val accessibilityService: SwitchifyAccessibilityService,
    private val gestureLockManager: GestureLockManager
) {

    private var startPoint: PointF? = null
    private var isPerformingGesture = false
    private var currentMultiPointGestureType: MultiPointGestureType? = null

    /**
     * Represents the types of gestures that can be performed.
     */
    enum class MultiPointGestureType {
        DRAG,
        CUSTOM_SWIPE,
        SWIPE_UP,
        SWIPE_DOWN,
        SWIPE_LEFT,
        SWIPE_RIGHT
    }

    /**
     * Starts a gesture of the specified type.
     *
     * @param type The type of gesture to start.
     */
    fun startGesture(type: MultiPointGestureType) {
        startPoint = GesturePoint.getPoint()
        isPerformingGesture = true
        currentMultiPointGestureType = type

        when (type) {
            MultiPointGestureType.DRAG -> {
                ServiceMessageHUD.instance.showMessage(
                    "Select where to drag to",
                    ServiceMessageHUD.MessageType.DISAPPEARING
                )
            }

            MultiPointGestureType.CUSTOM_SWIPE -> {
                ServiceMessageHUD.instance.showMessage(
                    "Select where to swipe to",
                    ServiceMessageHUD.MessageType.DISAPPEARING
                )
            }

            else -> {} // No message needed for regular swipes
        }

        gestureLockManager.informCannotLockGesture(GestureData.typeFromMultiPointGestureType(type))
    }

    /**
     * Ends the current gesture.
     */
    fun endGesture() {
        isPerformingGesture = false

        val startPoint = this.startPoint
        val gestureType = this.currentMultiPointGestureType
        if (startPoint == null || gestureType == null) {
            return
        }

        val endPoint = when (gestureType) {
            MultiPointGestureType.DRAG, MultiPointGestureType.CUSTOM_SWIPE -> GesturePoint.getPoint()
            else -> calculateSwipeEndPoint(gestureType, startPoint)
        }

        performGesture(gestureType, startPoint, endPoint)

        gestureLockManager.setLockedGestureData(
            GestureData.fromMultiPointGestureType(
                gestureType,
                startPoint,
                endPoint
            )
        )

        this.startPoint = null
        this.currentMultiPointGestureType = null
    }

    private fun calculateSwipeEndPoint(type: MultiPointGestureType, start: PointF): PointF {
        val screenWidth = ScreenUtils.getWidth(accessibilityService)
        val screenHeight = ScreenUtils.getHeight(accessibilityService)
        return when (type) {
            MultiPointGestureType.SWIPE_UP -> PointF(start.x, start.y - screenHeight / 5f)
            MultiPointGestureType.SWIPE_DOWN -> PointF(start.x, start.y + screenHeight / 5f)
            MultiPointGestureType.SWIPE_LEFT -> PointF(start.x - screenWidth / 4f, start.y)
            MultiPointGestureType.SWIPE_RIGHT -> PointF(start.x + screenWidth / 4f, start.y)
            else -> start // This shouldn't happen, but we need to handle all cases
        }
    }

    private fun performGesture(type: MultiPointGestureType, start: PointF, end: PointF) {
        val path = Path()
        path.moveTo(start.x, start.y)
        path.lineTo(end.x, end.y)

        val gestureDrawing = GestureDrawing(accessibilityService)
        gestureDrawing.drawLineAndArrowAndRemove(
            start.x.toInt(),
            start.y.toInt(),
            end.x.toInt(),
            end.y.toInt(),
            500
        )

        val duration = when (type) {
            MultiPointGestureType.DRAG -> DRAG_DURATION
            else -> SWIPE_DURATION
        }

        val gestureDescription = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        accessibilityService.dispatchGesture(
            gestureDescription,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    // Log completion if needed
                }
            },
            null
        )
    }

    /**
     * Checks if a gesture is currently being performed.
     *
     * @return True if a gesture is in progress, false otherwise.
     */
    fun isPerformingGesture(): Boolean = isPerformingGesture
}