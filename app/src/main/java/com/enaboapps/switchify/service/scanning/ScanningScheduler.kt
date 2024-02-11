package com.enaboapps.switchify.service.scanning

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScanningScheduler(private val onScan: suspend () -> Unit) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var scanningJob: Job? = null
    private var isPaused = false
    private var initialDelay: Long = 0L
    private var period: Long = 1000L // Default period of 1 second

    fun startScanning(initialDelay: Long, period: Long) {
        this.initialDelay = initialDelay
        this.period = period
        scanningJob?.cancel() // Cancel any existing job
        scanningJob = coroutineScope.launch {
            delay(initialDelay) // Apply initial delay before starting the scanning
            while (isActive) {
                onScan()
                delay(period)
            }
        }
    }

    fun stopScanning() {
        scanningJob?.cancel()
    }

    fun pauseScanning() {
        isPaused = true
        scanningJob?.cancel() // Cancel the job but keep the configuration for resuming
    }

    fun resumeScanning() {
        if (isPaused) {
            isPaused = false
            // Resume with the original period, but without the initial delay
            scanningJob = coroutineScope.launch {
                delay(period) // Delay for the period before resuming regular scanning
                while (isActive) {
                    onScan()
                    delay(period)
                }
            }
        }
    }

    fun shutdown() {
        coroutineScope.cancel() // Cancel all coroutines started by this scope
    }
}