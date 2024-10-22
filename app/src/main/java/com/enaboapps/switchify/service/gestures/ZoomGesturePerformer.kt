package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import com.enaboapps.switchify.service.gestures.data.GestureType
import com.enaboapps.switchify.service.gestures.utils.GestureUtils.getInBoundsCoordinate
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.math.abs

object ZoomGesturePerformer {

    private const val TAG = "ZoomGesturePerformer"
    private const val DEFAULT_ZOOM_DURATION = 500L // Adjusted duration in milliseconds
    private const val ZOOM_AMOUNT_DP = 200 // Zoom amount in density-independent pixels (dp)
    private const val VERTICAL_OFFSET_DP = 100 // Vertical offset for natural gesture

    /**
     * Perform a zoom action.
     *
     * @param type The type of zoom action (ZOOM_IN or ZOOM_OUT).
     * @param accessibilityService The accessibility service used to dispatch gestures.
     */
    fun performZoomAction(type: GestureType, accessibilityService: AccessibilityService) {
        // Retrieve the center point for the zoom gesture
        val centerPoint = GesturePoint.getPoint()
        Log.d(TAG, "Center Point: (${centerPoint.x}, ${centerPoint.y})")

        // Calculate zoom amount based on screen density
        val density = accessibilityService.resources.displayMetrics.density
        val zoomAmountPx = (ZOOM_AMOUNT_DP * density).toInt()
        Log.d(TAG, "Zoom Amount (px): $zoomAmountPx")

        // Calculate vertical offset for more natural finger placement
        val verticalOffsetPx = (VERTICAL_OFFSET_DP * density).toInt()
        Log.d(TAG, "Vertical Offset (px): $verticalOffsetPx")

        // Initialize gesture paths for the two fingers
        val path1 = Path()
        val path2 = Path()

        // Obtain screen width to ensure zoom points are within bounds
        val screenWidth = ScreenUtils.getWidth(accessibilityService)
        val halfZoomAmount = zoomAmountPx / 2

        // Calculate initial left and right zoom points relative to the center
        var leftZoomPoint = getInBoundsCoordinate(screenWidth, centerPoint.x - halfZoomAmount)
        var rightZoomPoint = getInBoundsCoordinate(screenWidth, centerPoint.x + halfZoomAmount)
        Log.d(
            TAG,
            "Initial Left Zoom Point: $leftZoomPoint, Initial Right Zoom Point: $rightZoomPoint"
        )

        // Ensure that both zoom points are equidistant from the center
        val leftDistance = abs(centerPoint.x - leftZoomPoint)
        val rightDistance = abs(centerPoint.x - rightZoomPoint)
        if (leftDistance != rightDistance) {
            val distanceDifference = abs(leftDistance - rightDistance)
            if (leftDistance > rightDistance) {
                leftZoomPoint -= distanceDifference
            } else {
                rightZoomPoint += distanceDifference // Changed to += for proper symmetry
            }
            Log.d(
                TAG,
                "Adjusted Left Zoom Point: $leftZoomPoint, Adjusted Right Zoom Point: $rightZoomPoint"
            )
        }

        // Define gesture paths based on the type of zoom action
        when (type) {
            GestureType.ZOOM_IN -> {
                // Fingers move outward from the center for zooming in
                path1.moveTo(centerPoint.x.toFloat(), (centerPoint.y - verticalOffsetPx).toFloat())
                path1.lineTo(leftZoomPoint.toFloat(), (centerPoint.y - verticalOffsetPx).toFloat())

                path2.moveTo(centerPoint.x.toFloat(), (centerPoint.y + verticalOffsetPx).toFloat())
                path2.lineTo(rightZoomPoint.toFloat(), (centerPoint.y + verticalOffsetPx).toFloat())
            }

            GestureType.ZOOM_OUT -> {
                // Fingers move inward towards the center for zooming out
                path1.moveTo(leftZoomPoint.toFloat(), (centerPoint.y - verticalOffsetPx).toFloat())
                path1.lineTo(centerPoint.x.toFloat(), (centerPoint.y - verticalOffsetPx).toFloat())

                path2.moveTo(rightZoomPoint.toFloat(), (centerPoint.y + verticalOffsetPx).toFloat())
                path2.lineTo(centerPoint.x.toFloat(), (centerPoint.y + verticalOffsetPx).toFloat())
            }

            else -> {
                Log.e(TAG, "performZoomAction: Invalid zoom type: $type")
                return
            }
        }

        // Build the gesture description with the defined paths and duration
        val gestureBuilder = GestureDescription.Builder()
        val stroke1 = GestureDescription.StrokeDescription(path1, 0, DEFAULT_ZOOM_DURATION)
        val stroke2 = GestureDescription.StrokeDescription(path2, 0, DEFAULT_ZOOM_DURATION)
        gestureBuilder.addStroke(stroke1).addStroke(stroke2)

        val gestureDescription = gestureBuilder.build()

        // Dispatch the gesture using the accessibility service
        try {
            accessibilityService.dispatchGesture(
                gestureDescription,
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        Log.d(TAG, "Gesture Completed Successfully")
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        Log.e(TAG, "Gesture Cancelled")
                    }
                },
                null
            )
            Log.d(TAG, "Gesture dispatched: $type")
        } catch (e: Exception) {
            Log.e(TAG, "performZoomAction: Exception during gesture dispatch", e)
        }
    }
}