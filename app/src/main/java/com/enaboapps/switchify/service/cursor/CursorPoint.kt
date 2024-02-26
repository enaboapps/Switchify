package com.enaboapps.switchify.service.cursor

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import com.enaboapps.switchify.service.utils.ScreenUtils

interface CursorPointListener {
    fun onCursorPointReselect()
}

class CursorPoint {
    companion object {
        val instance: CursorPoint by lazy {
            CursorPoint()
        }
    }

    var listener: CursorPointListener? = null

    var x = 0
    var y = 0

    var lastXQuadrant = QuadrantInfo(0, 0, 0)
    var lastYQuadrant = QuadrantInfo(0, 0, 0)

    fun getPoint(): PointF {
        return PointF(x.toFloat(), y.toFloat())
    }

    // This function returns the whole screen rect
    fun getRectForScreen(context: Context): Rect {
        return Rect(0, 0, ScreenUtils.getWidth(context), ScreenUtils.getHeight(context))
    }

    // This function sets the reselect flag
    fun setReselect(reselect: Boolean) {
        if (reselect) {
            listener?.onCursorPointReselect()
        }
    }
}