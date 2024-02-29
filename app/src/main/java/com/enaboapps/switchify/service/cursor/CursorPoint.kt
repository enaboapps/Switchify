package com.enaboapps.switchify.service.cursor

import android.graphics.PointF

/**
 * This interface represents the cursor point listener
 */
interface CursorPointListener {
    fun onCursorPointReselect()
}

/**
 * This object represents the cursor point
 */
object CursorPoint {
    var listener: CursorPointListener? = null

    /**
     * This is the x (horizontal) position of the cursor
     */
    var x = 0

    /**
     * This is the y (vertical) position of the cursor
     */
    var y = 0

    var lastXQuadrant = QuadrantInfo(0, 0, 0)
    var lastYQuadrant = QuadrantInfo(0, 0, 0)

    /**
     * This function returns the current cursor point
     * @return The current cursor point
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
            listener?.onCursorPointReselect()
        }
    }
}