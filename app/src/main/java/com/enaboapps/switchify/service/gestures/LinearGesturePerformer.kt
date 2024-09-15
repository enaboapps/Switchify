package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.data.GestureData
import com.enaboapps.switchify.service.gestures.data.GestureData.Companion.DRAG_DURATION
import com.enaboapps.switchify.service.gestures.data.GestureData.Companion.SCROLL_DURATION
import com.enaboapps.switchify.service.gestures.data.GestureData.Companion.SWIPE_DURATION
import com.enaboapps.switchify.service.gestures.data.GestureType
import com.enaboapps.switchify.service.gestures.visuals.GestureDrawing
import com.enaboapps.switchify.service.utils.ScreenUtils
import com.enaboapps.switchify.service.window.ServiceMessageHUD

/**
 * A class that performs linear gestures.
 *
 * @property accessibilityService The accessibility service to use for performing gestures.
 * @property gestureLockManager The gesture lock manager to use for locking gestures.
 */
class LinearGesturePerformer(
    private val accessibilityService: SwitchifyAccessibilityService,
    private val gestureLockManager: GestureLockManager
) {

    private var startPoint: PointF? = null
    private var isPerformingGesture = false
    private var currentGestureType: GestureType? = null

    /**
     * Starts a gesture of the specified type.
     *
     * @param type The type of gesture to start.
     */
    fun startGesture(type: GestureType) {
        startPoint = GesturePoint.getPoint()
        isPerformingGesture = true
        currentGestureType = type

        when (type) {
            GestureType.DRAG -> {
                ServiceMessageHUD.instance.showMessage(
                    "Select where to drag to",
                    ServiceMessageHUD.MessageType.DISAPPEARING
                )
            }

            GestureType.CUSTOM_SWIPE -> {
                ServiceMessageHUD.instance.showMessage(
                    "Select where to swipe to",
                    ServiceMessageHUD.MessageType.DISAPPEARING
                )
            }

            else -> {} // No message needed for regular swipes
        }

        gestureLockManager.informCannotLockGesture(type)
    }

    /**
     * Ends the current gesture.
     */
    fun endGesture() {
        isPerformingGesture = false

        val startPoint = this.startPoint
        val gestureType = this.currentGestureType
        if (startPoint == null || gestureType == null) {
            return
        }

        val endPoint = when (gestureType) {
            GestureType.DRAG, GestureType.CUSTOM_SWIPE -> GesturePoint.getPoint()
            else -> calculateEndPoint(gestureType, startPoint)
        }

        performGesture(gestureType, startPoint, endPoint)

        gestureLockManager.setLockedGestureData(
            GestureData(
                gestureType,
                startPoint,
                endPoint
            )
        )

        this.startPoint = null
        this.currentGestureType = null
    }

    private fun calculateEndPoint(type: GestureType, start: PointF): PointF {
        val screenWidth = ScreenUtils.getWidth(accessibilityService)
        val screenHeight = ScreenUtils.getHeight(accessibilityService)
        return when (type) {
            GestureType.SWIPE_UP, GestureType.SCROLL_UP -> PointF(
                start.x,
                start.y - screenHeight / 5f
            )

            GestureType.SWIPE_DOWN, GestureType.SCROLL_DOWN -> PointF(
                start.x,
                start.y + screenHeight / 5f
            )

            GestureType.SWIPE_LEFT, GestureType.SCROLL_LEFT -> PointF(
                start.x - screenWidth / 4f,
                start.y
            )

            GestureType.SWIPE_RIGHT, GestureType.SCROLL_RIGHT -> PointF(
                start.x + screenWidth / 4f,
                start.y
            )

            else -> start // This shouldn't happen, but we need to handle all cases
        }
    }

    private fun performGesture(type: GestureType, start: PointF, end: PointF) {
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
            GestureType.DRAG -> DRAG_DURATION
            GestureType.SCROLL_UP, GestureType.SCROLL_DOWN, GestureType.SCROLL_LEFT, GestureType.SCROLL_RIGHT -> SCROLL_DURATION
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