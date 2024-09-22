package com.enaboapps.switchify.service.radar

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import kotlin.math.cos
import kotlin.math.sin

class RadarManager(private val context: Context) : ScanStateInterface {

    companion object {
        private const val TAG = "RadarManager"
        private const val FULL_CIRCLE = 360f
        private const val ROTATION_STEP = 1f
        private const val MOVEMENT_STEP = 5f
    }

    private val scanSettings = ScanSettings(context)
    private val uiHandler = Handler(Looper.getMainLooper())
    private val radarUI = RadarUI(context, uiHandler)

    private var currentAngle = 0f
    private var currentDistance = 0f
    private var scanningScheduler: ScanningScheduler? = null

    private val screenCenterX: Float
        get() = context.resources.displayMetrics.widthPixels / 2f
    private val screenCenterY: Float
        get() = context.resources.displayMetrics.heightPixels / 2f
    private val maxDistance: Float
        get() = minOf(screenCenterX, screenCenterY) * 0.9f

    private var isRotating = true

    init {
        setup()
    }

    private fun setup() {
        if (scanningScheduler == null) {
            scanningScheduler = ScanningScheduler(context) { update() }
        }
    }

    private fun update() {
        if (isRotating) {
            rotate()
        } else {
            moveCircle()
        }
    }

    private fun rotate() {
        currentAngle = (currentAngle + ROTATION_STEP) % FULL_CIRCLE
        updateRadarLine()
    }

    private fun moveCircle() {
        currentDistance += MOVEMENT_STEP
        if (currentDistance > maxDistance) {
            currentDistance = 0f
        }
        updateRadarCircle()
    }

    private fun updateRadarLine() {
        val endX =
            screenCenterX + maxDistance * cos(Math.toRadians(currentAngle.toDouble())).toFloat()
        val endY =
            screenCenterY + maxDistance * sin(Math.toRadians(currentAngle.toDouble())).toFloat()
        radarUI.showRadarLine(endX.toInt(), endY.toInt())
    }

    private fun updateRadarCircle() {
        val angle = Math.toRadians(currentAngle.toDouble())
        val x = screenCenterX + currentDistance * cos(angle).toFloat()
        val y = screenCenterY + currentDistance * sin(angle).toFloat()
        radarUI.showRadarCircle(x.toInt(), y.toInt())
    }

    fun startRadar() {
        updateRadarLine()
        startAutoScanIfEnabled()
    }

    private fun startAutoScanIfEnabled() {
        if (scanSettings.isAutoScanMode()) {
            val rate = scanSettings.getScanRate()
            scanningScheduler?.startScanning(rate, rate)
        }
    }

    override fun stopScanning() {
        scanningScheduler?.stopScanning()
    }

    override fun pauseScanning() {
        scanningScheduler?.pauseScanning()
    }

    override fun resumeScanning() {
        scanningScheduler?.resumeScanning()
    }

    fun performSelectionAction() {
        if (isRotating) {
            isRotating = false
            currentDistance = 0f
            startAutoScanIfEnabled()
        } else {
            stopScanning()
            val angle = Math.toRadians(currentAngle.toDouble())
            val x = screenCenterX + currentDistance * cos(angle).toFloat()
            val y = screenCenterY + currentDistance * sin(angle).toFloat()
            GesturePoint.x = x.toInt()
            GesturePoint.y = y.toInt()
            resetRadar()
        }
    }

    fun resetRadar() {
        isRotating = true
        currentAngle = 0f
        currentDistance = 0f
        radarUI.reset()
        startAutoScanIfEnabled()
    }

    fun cleanup() {
        radarUI.reset()
        scanningScheduler?.shutdown()
        scanningScheduler = null
    }
}