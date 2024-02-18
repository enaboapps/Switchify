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
    private var isPaused = false
    private var isExecuting = false // Execution flag to track onScan execution
    private var initialDelay: Long = 0L
    private var period: Long = 1000L // Default period of 1 second

    fun startScanning(initialDelay: Long, period: Long) {
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

    fun stopScanning() {
        println("[$uniqueId] Stopping scanning job")
        scanningJob?.cancel()
    }

    fun pauseScanning() {
        isPaused = true
        println("[$uniqueId] Pausing scanning job")
        scanningJob?.cancel() // Cancel the job but keep the configuration for resuming
    }

    fun resumeScanning() {
        if (isPaused) {
            isPaused = false
            println("[$uniqueId] Resuming scanning job")
            // Resume with the original period, but without the initial delay
            scanningJob = coroutineScope.launch {
                delay(period) // Delay for the period before resuming regular scanning
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
    }

    fun shutdown() {
        println("[$uniqueId] Shutting down scope")
        coroutineScope.cancel() // Cancel all coroutines started by this scope
    }
}