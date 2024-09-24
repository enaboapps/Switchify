package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.data.GestureData
import com.enaboapps.switchify.service.gestures.data.GestureData.Companion.DOUBLE_TAP_INTERVAL
import com.enaboapps.switchify.service.gestures.data.GestureData.Companion.TAP_AND_HOLD_DURATION
import com.enaboapps.switchify.service.gestures.data.GestureData.Companion.TAP_DURATION
import com.enaboapps.switchify.service.gestures.data.GestureType
import com.enaboapps.switchify.service.gestures.visuals.GestureDrawing
import com.enaboapps.switchify.service.methods.nodes.NodeExaminer
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The GestureManager class is responsible for managing and performing various gesture actions
 * in the Switchify accessibility service. It handles taps, swipes, drags, and custom gestures.
 */
class GestureManager private constructor() {
    companion object {
        private var instance: GestureManager? = null

        /**
         * Gets the singleton instance of the GestureManager.
         *
         * @return The GestureManager instance.
         */
        fun getInstance(): GestureManager {
            if (instance == null) {
                instance = GestureManager()
            }
            return instance!!
        }
    }

    private var accessibilityService: SwitchifyAccessibilityService? = null
    private var gestureLockManager: GestureLockManager? = null
    private var preferenceManager: PreferenceManager? = null
    private lateinit var linearGesturePerformer: LinearGesturePerformer

    /**
     * Sets up the GestureManager with the necessary components.
     *
     * @param accessibilityService The SwitchifyAccessibilityService instance.
     */
    fun setup(accessibilityService: SwitchifyAccessibilityService) {
        this.accessibilityService = accessibilityService
        gestureLockManager = GestureLockManager()
        preferenceManager = PreferenceManager(accessibilityService)
        linearGesturePerformer =
            LinearGesturePerformer(accessibilityService, gestureLockManager!!)
    }

    /**
     * Checks if the current gesture point is close to the center of the screen.
     *
     * @return True if the point is within 350dp of the screen center, false otherwise.
     */
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

    /**
     * Gets the current gesture point, applying assisted selection if enabled.
     *
     * @return The PointF representing the current gesture point.
     */
    private fun getAssistedCurrentPoint(): PointF {
        return if (preferenceManager?.getBooleanValue(PreferenceManager.PREFERENCE_KEY_ASSISTED_SELECTION) == true) {
            NodeExaminer.getClosestNodeToPoint(GesturePoint.getPoint())
        } else {
            GesturePoint.getPoint()
        }
    }

    /**
     * Performs a tap gesture at the current point.
     */
    fun performTap() {
        try {
            accessibilityService?.let {
                val path = Path()
                val point = getAssistedCurrentPoint()
                val gestureDrawing = GestureDrawing(it)
                gestureDrawing.drawCircleAndRemove(
                    point.x.toInt(),
                    point.y.toInt(),
                    TAP_DURATION
                )
                path.moveTo(point.x, point.y)
                gestureLockManager?.setLockedGestureData(
                    GestureData(
                        GestureType.TAP,
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

    /**
     * Performs a double tap gesture at the current point.
     */
    fun performDoubleTap() {
        try {
            accessibilityService?.let {
                val path = Path()
                val point = getAssistedCurrentPoint()
                val gestureDrawing = GestureDrawing(it)
                gestureDrawing.drawCircleAndRemove(
                    point.x.toInt(),
                    point.y.toInt(),
                    TAP_DURATION
                )
                path.moveTo(point.x, point.y)
                gestureLockManager?.setLockedGestureData(
                    GestureData(
                        GestureType.DOUBLE_TAP,
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
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onDoubleTap: ", e)
        }
    }

    /**
     * Performs a tap and hold gesture at the current point.
     */
    fun performTapAndHold() {
        try {
            accessibilityService?.let {
                val path = Path()
                val point = getAssistedCurrentPoint()
                val gestureDrawing = GestureDrawing(it)
                gestureDrawing.drawCircleAndRemove(
                    point.x.toInt(),
                    point.y.toInt(),
                    TAP_AND_HOLD_DURATION
                )
                path.moveTo(point.x, point.y)
                gestureLockManager?.setLockedGestureData(
                    GestureData(
                        GestureType.TAP_AND_HOLD,
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
                        }
                    },
                    null
                )
            }
        } catch (e: Exception) {
            // Log.e(TAG, "onTapAndHold: ", e)
        }
    }

    /**
     * Performs the gesture lock action if the gesture lock is enabled.
     *
     * @return True if a locked gesture action was performed, false otherwise.
     */
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

    /**
     * Toggles the gesture lock on or off.
     */
    fun toggleGestureLock() {
        gestureLockManager?.toggleGestureLock()
    }

    /**
     * Checks if the gesture lock is currently enabled.
     *
     * @return True if the gesture lock is enabled, false otherwise.
     */
    fun isGestureLockEnabled(): Boolean {
        return gestureLockManager?.isGestureLockEnabled() == true
    }

    /**
     * Performs a swipe or scroll action.
     *
     * @param type The GestureType of the swipe.
     */
    fun performSwipeOrScroll(type: GestureType) {
        linearGesturePerformer.startGesture(type)
        linearGesturePerformer.endGesture()
    }

    /**
     * Starts a drag gesture.
     */
    fun startDragGesture() {
        linearGesturePerformer.startGesture(GestureType.DRAG)
        ScanMethod.setType(ScanMethod.MethodType.CURSOR)
    }

    /**
     * Starts a custom swipe gesture.
     */
    fun startCustomSwipe() {
        linearGesturePerformer.startGesture(GestureType.CUSTOM_SWIPE)
        ScanMethod.setType(ScanMethod.MethodType.CURSOR)
    }

    /**
     * Ends a linear gesture.
     */
    fun endLinearGesture() {
        linearGesturePerformer.endGesture()
    }

    /**
     * Checks if a linear gesture is currently being performed.
     *
     * @return True if a linear gesture is being performed, false otherwise.
     */
    fun isPerformingLinearGesture(): Boolean {
        return linearGesturePerformer.isPerformingGesture()
    }

    /**
     * Performs a zoom action.
     *
     * @param type The type of zoom action to perform.
     */
    fun performZoom(type: GestureType) {
        gestureLockManager?.setLockedGestureData(
            GestureData(
                type,
                GesturePoint.getPoint()
            )
        )
        accessibilityService?.let {
            ZoomGesturePerformer.performZoomAction(type, it)
        }
    }
}