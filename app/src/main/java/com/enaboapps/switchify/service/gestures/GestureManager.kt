package com.enaboapps.switchify.service.gestures

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.PointF
import com.enaboapps.switchify.service.SwitchifyAccessibilityService

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
            path.moveTo(currentPoint!!.x, currentPoint!!.y)
            val gestureDescription = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 550, 100)).build()
            accessibilityService?.dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    // Log.d(TAG, "onCompleted")
                }
            }, null)
        } catch (e: Exception) {
            // Log.e(TAG, "onTap: ", e)
        }
    }
}