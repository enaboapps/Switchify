package com.enaboapps.switchify.service.scanning

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.UUID

class ScanningScheduler(private val onScan: suspend () -> Unit) {
    private val uniqueId = UUID.randomUUID().toString()

    // Create a new single-threaded context named "ScanningThread"
    @OptIn(DelicateCoroutinesApi::class)
    private val singleThreadContext = newSingleThreadContext("ScanningThread-$uniqueId")

    // Use the single-threaded context for the coroutineScope
    private val coroutineScope = CoroutineScope(singleThreadContext + CoroutineName(uniqueId))

    private var scanningJob: Job? = null
    private var isPaused = false
    private var isExecuting = false
    private var initialDelay: Long = 0L
    private var period: Long = 1000L

    fun startScanning(initialDelay: Long, period: Long) {
        this.initialDelay = initialDelay
        this.period = period
        scanningJob?.cancel()
        scanningJob = coroutineScope.launch {
            println("[$uniqueId] Starting scanning job")
            delay(initialDelay)
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
        scanningJob?.cancel()
    }

    fun resumeScanning() {
        if (isPaused) {
            isPaused = false
            println("[$uniqueId] Resuming scanning job")
            startScanning(initialDelay, period)
        }
    }

    fun shutdown() {
        println("[$uniqueId] Shutting down scope")
        coroutineScope.cancel() // Cancel all coroutines started by this scope
        singleThreadContext.close() // Properly close the single-thread context to avoid memory leaks
    }
}