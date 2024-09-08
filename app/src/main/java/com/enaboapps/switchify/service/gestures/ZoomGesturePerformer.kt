package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import com.enaboapps.switchify.service.gestures.data.GestureType
import com.enaboapps.switchify.service.gestures.utils.GestureUtils.getInBoundsCoordinate
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.math.abs

object ZoomGesturePerformer {

    private const val ZOOM_DURATION = 1000L
    private const val ZOOM_AMOUNT = 500

    /**
     * Perform a zoom action
     * @param type The type of zoom action
     * @param accessibilityService The accessibility service
     */
    fun performZoomAction(type: GestureType, accessibilityService: AccessibilityService) {
        val centerPoint = GesturePoint.getPoint()

        // Initialize paths for the two fingers
        val path1 = android.graphics.Path()
        val path2 = android.graphics.Path()

        // Figure out the zoom point
        var leftZoomPoint = getInBoundsCoordinate(
            ScreenUtils.getWidth(accessibilityService),
            centerPoint.x - ZOOM_AMOUNT
        )
        var rightZoomPoint = getInBoundsCoordinate(
            ScreenUtils.getWidth(accessibilityService),
            centerPoint.x + ZOOM_AMOUNT
        )

        // Make sure that the zoom points are equal distance from the center point
        val leftDistance = abs(centerPoint.x - leftZoomPoint)
        val rightDistance = abs(centerPoint.x - rightZoomPoint)
        if (leftDistance != rightDistance) {
            val distanceDifference = abs(leftDistance - rightDistance)
            if (leftDistance > rightDistance) {
                leftZoomPoint -= distanceDifference
            } else {
                rightZoomPoint -= distanceDifference
            }
        }

        when (type) {
            GestureType.ZOOM_IN -> {
                // Setup paths for zooming in (fingers moving apart)
                path1.moveTo(centerPoint.x, centerPoint.y)
                path1.lineTo(leftZoomPoint, centerPoint.y)

                path2.moveTo(centerPoint.x, centerPoint.y)
                path2.lineTo(rightZoomPoint, centerPoint.y)
            }

            GestureType.ZOOM_OUT -> {
                // Setup paths for zooming out (fingers moving together)
                path1.moveTo(leftZoomPoint, centerPoint.y)
                path1.lineTo(centerPoint.x, centerPoint.y)

                path2.moveTo(rightZoomPoint, centerPoint.y)
                path2.lineTo(centerPoint.x, centerPoint.y)
            }

            else -> {
                // Log.e(TAG, "performZoomAction: Invalid zoom type")
                return
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
        } catch (e: Exception) {
            // Log.e(TAG, "onZoom: ", e)
        }
    }

}