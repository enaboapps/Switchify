package com.enaboapps.switchify.service.radar

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Handler
import android.view.WindowManager
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanColorManager
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class RadarUI(private val context: Context, private val handler: Handler) {
    private var radarLine: RelativeLayout? = null
    private var radarCircle: RelativeLayout? = null

    private val window = SwitchifyAccessibilityWindow.instance
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    companion object {
        const val RADAR_LINE_THICKNESS = 5
        const val RADAR_CIRCLE_SIZE = 20
        private const val RADAR_ALPHA = 0.7f
    }

    private val screenSize = Point()

    init {
        updateScreenSize()
    }

    private fun updateScreenSize() {
        windowManager.defaultDisplay.getRealSize(screenSize)
    }

    private val screenCenterX: Int
        get() = screenSize.x / 2

    private val screenCenterY: Int
        get() = screenSize.y / 2

    fun showRadarLine(endX: Int, endY: Int) {
        updateScreenSize() // Ensure we have the latest screen size
        if (radarLine == null) {
            createRadarLine(endX, endY)
        } else {
            updateRadarLine(endX, endY)
        }
    }

    fun showRadarCircle(x: Int, y: Int) {
        updateScreenSize() // Ensure we have the latest screen size
        if (radarCircle == null) {
            createRadarCircle(x, y)
        } else {
            updateRadarCircle(x, y)
        }
    }

    private fun createRadarLine(endX: Int, endY: Int) {
        val length = sqrt(
            (endX - screenCenterX).toDouble().pow(2.0) +
                    (endY - screenCenterY).toDouble().pow(2.0)
        ).toInt()

        radarLine = RelativeLayout(context).apply {
            val color = ScanColorManager.getScanColorSetFromPreferences(context).secondaryColor
            setBackgroundColor(Color.parseColor(color))
            alpha = RADAR_ALPHA
        }
        handler.post {
            radarLine?.let {
                window.addView(it, screenCenterX, screenCenterY, length, RADAR_LINE_THICKNESS)
                it.rotation = calculateAngle(endX, endY).toFloat()
            }
        }
    }

    private fun createRadarCircle(x: Int, y: Int) {
        radarCircle = RelativeLayout(context).apply {
            val color = ScanColorManager.getScanColorSetFromPreferences(context).primaryColor
            setBackgroundColor(Color.parseColor(color))
            alpha = RADAR_ALPHA
        }
        handler.post {
            radarCircle?.let {
                window.addView(
                    it,
                    x - RADAR_CIRCLE_SIZE / 2,
                    y - RADAR_CIRCLE_SIZE / 2,
                    RADAR_CIRCLE_SIZE,
                    RADAR_CIRCLE_SIZE
                )
            }
        }
    }

    private fun updateRadarLine(endX: Int, endY: Int) {
        val length = sqrt(
            (endX - screenCenterX).toDouble().pow(2.0) +
                    (endY - screenCenterY).toDouble().pow(2.0)
        ).toInt()

        radarLine?.let {
            handler.post {
                window.updateViewLayout(it, screenCenterX, screenCenterY)
                it.rotation = calculateAngle(endX, endY).toFloat()
            }
        }
    }

    private fun updateRadarCircle(x: Int, y: Int) {
        radarCircle?.let {
            handler.post {
                window.updateViewLayout(it, x - RADAR_CIRCLE_SIZE / 2, y - RADAR_CIRCLE_SIZE / 2)
            }
        }
    }

    private fun removeRadarLine() {
        radarLine?.let {
            handler.post {
                window.removeView(it)
            }
            radarLine = null
        }
    }

    private fun removeRadarCircle() {
        radarCircle?.let {
            handler.post {
                window.removeView(it)
            }
            radarCircle = null
        }
    }

    fun isReset(): Boolean {
        return radarLine == null && radarCircle == null
    }

    fun reset() {
        removeRadarLine()
        removeRadarCircle()
    }

    fun isRadarLineVisible(): Boolean {
        return radarLine != null
    }

    fun isRadarCircleVisible(): Boolean {
        return radarCircle != null
    }

    private fun calculateAngle(endX: Int, endY: Int): Double {
        val dx = endX - screenCenterX
        val dy = endY - screenCenterY
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        if (angle < 0) {
            angle += 360.0
        }
        return angle
    }
}