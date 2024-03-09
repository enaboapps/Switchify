package com.enaboapps.switchify.service.gestures

import android.graphics.PointF
import com.enaboapps.switchify.service.cursor.QuadrantInfo
import com.enaboapps.switchify.service.scanning.ScanMethod

/**
 * This interface represents the gesture point listener
 */
interface GesturePointListener {
    fun onGesturePointReselect()
}

/**
 * This object represents the cursor point
 */
object GesturePoint {
    var listener: GesturePointListener? = null

    /**
     * This is the x (horizontal) position of the gesture point
     */
    var x = 0

    /**
     * This is the y (vertical) position of the gesture point
     */
    var y = 0

    var lastXQuadrant = QuadrantInfo(0, 0, 0)
    var lastYQuadrant = QuadrantInfo(0, 0, 0)

    /**
     * This function returns the current gesture point
     * @return The gesture point
     */
    fun getPoint(): PointF {
        return PointF(x.toFloat(), y.toFloat())
    }

    /**
     * This function sets the cursor to reselect
     * @param reselect The reselect value
     */
    fun setReselect(reselect: Boolean) {
        if (reselect) {
            ScanMethod.setType(ScanMethod.MethodType.CURSOR)
            listener?.onGesturePointReselect()
        }
    }
}