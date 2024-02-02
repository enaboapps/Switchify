package com.enaboapps.switchify.service.gestures.visuals

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class GestureIndicatorView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF0000.toInt() // Red color for the gesture indicator
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private var path = Path()

    fun setGesture(startX: Float, startY: Float, endX: Float, endY: Float) {
        path.reset()
        // Line from start to end
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)

        // Calculate arrow head
        val arrowHeadLength = 40f // Length of the arrow head
        val arrowAngle = Math.toRadians(45.0) // Angle of the arrow head in radians

        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
        // Left side of the arrow
        path.lineTo(
            (endX - arrowHeadLength * cos(angle - arrowAngle)).toFloat(),
            (endY - arrowHeadLength * sin(angle - arrowAngle)).toFloat()
        )
        path.moveTo(endX, endY)
        // Right side of the arrow
        path.lineTo(
            (endX - arrowHeadLength * cos(angle + arrowAngle)).toFloat(),
            (endY - arrowHeadLength * sin(angle + arrowAngle)).toFloat()
        )

        invalidate() // Redraw the view with the new path
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }
}