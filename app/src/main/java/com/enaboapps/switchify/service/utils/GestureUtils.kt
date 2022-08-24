package com.enaboapps.switchify.service.utils

import android.accessibilityservice.GestureDescription
import android.graphics.PointF

class GestureUtils {

    fun createTap(point: PointF): GestureDescription {
        val path = android.graphics.Path()
        path.moveTo(point.x, point.y)
        return GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 100, 100))
                .build()
    }

}