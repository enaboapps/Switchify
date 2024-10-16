package com.enaboapps.switchify.service.gestures.visuals

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanColorManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class GestureIndicatorView(context: Context) : RelativeLayout(context) {
    private val drawingView: DrawingView

    init {
        // Make the RelativeLayout transparent
        setBackgroundColor(Color.TRANSPARENT)

        // Create and add the drawing view
        drawingView = DrawingView(context)
        addView(drawingView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setGesture(startX: Float, startY: Float, endX: Float, endY: Float) {
        drawingView.setGesture(startX, startY, endX, endY)
    }

    private inner class DrawingView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color =
                Color.parseColor(ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor)
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
}