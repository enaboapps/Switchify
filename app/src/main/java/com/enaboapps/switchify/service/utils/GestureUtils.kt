package com.enaboapps.switchify.service.utils

import android.accessibilityservice.GestureDescription
import android.graphics.PointF

val TAP_TIMEOUT = 550L

class GestureUtils {

    fun createTap(point: PointF): GestureDescription {
        val path = android.graphics.Path()
        path.moveTo(point.x, point.y)
        return GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, TAP_TIMEOUT, 100))
                .build()
    }

}