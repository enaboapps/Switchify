package com.enaboapps.switchify.service.scanning

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class ScanningScheduler(private val onScan: suspend () -> Unit) {
    // Generate a unique identifier for the scope
    private val uniqueId = UUID.randomUUID().toString()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName(uniqueId))
    private var scanningJob: Job? = null
    private var isExecuting = false // Execution flag to track onScan execution
    private var initialDelay: Long = 0L
    private var period: Long = 1000L // Default period of 1 second

    private var scanState = ScanState.STOPPED

    fun startScanning(initialDelay: Long, period: Long) {
        if (scanState == ScanState.SCANNING) {
            return
        }
        scanState = ScanState.SCANNING
        this.initialDelay = initialDelay
        this.period = period
        scanningJob?.cancel() // Cancel any existing job
        scanningJob = coroutineScope.launch {
            println("[$uniqueId] Starting scanning job")
            delay(initialDelay) // Apply initial delay before starting the scanning
            while (isActive) {
                if (!isExecuting) {
                    isExecuting = true
                    try {
                        onScan()
                    } finally {
                        isExecuting = false
                    }
                }
                delay(period)
            }
        }
    }

    fun isScanning(): Boolean {
        return scanState == ScanState.SCANNING
    }

    fun isPaused(): Boolean {
        return scanState == ScanState.PAUSED
    }

    fun isStopped(): Boolean {
        return scanState == ScanState.STOPPED
    }

    fun stopScanning() {
        scanState = ScanState.STOPPED
        scanningJob?.cancel()
    }

    fun pauseScanning() {
        scanState = ScanState.PAUSED
        scanningJob?.cancel()
    }

    fun resumeScanning() {
        startScanning(initialDelay, period)
    }

    fun shutdown() {
        println("[$uniqueId] Shutting down scope")
        coroutineScope.cancel() // Cancel all coroutines started by this scope
    }
}