package com.enaboapps.switchify.service.cursor

import android.graphics.PointF

class CursorPoint {
    companion object {
        val instance: CursorPoint by lazy {
            CursorPoint()
        }
    }

    var point: PointF? = null
}