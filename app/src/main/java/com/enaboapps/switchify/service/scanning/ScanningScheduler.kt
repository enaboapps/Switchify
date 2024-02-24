package com.enaboapps.switchify.service.scanning

import android.content.Context
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ScanningScheduler(context: Context, private val onScan: suspend () -> Unit) {
    private val uniqueId = UUID.randomUUID().toString()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName(uniqueId))
    private var scanningJob: Job? = null
    private val isExecuting = AtomicBoolean(false)
    private var initialDelay: Long = 0L
    private var period: Long = 1000L // Default period of 1 second

    private var scanState = AtomicReference(ScanState.STOPPED)

    private val scanSettings = ScanSettings(context)

    fun startScanning(
        initialDelay: Long = scanSettings.getScanRate(),
        period: Long = scanSettings.getScanRate()
    ) {
        if (scanState.get() == ScanState.SCANNING) {
            println("[$uniqueId] Already scanning")
            return
        }
        scanState.set(ScanState.SCANNING)
        val initialDelayPlusPause = initialDelay + scanSettings.getPauseOnFirstItemDelay()
        this.initialDelay = initialDelay
        this.period = period
        scanningJob?.cancel() // Ensure no previous job is running
        scanningJob = coroutineScope.launch {
            println("[$uniqueId] Starting scanning job")
            delay(initialDelayPlusPause)
            while (isActive) {
                if (isExecuting.compareAndSet(false, true)) {
                    try {
                        onScan()
                    } catch (e: Exception) {
                        println("Error during scan: ${e.message}")
                    } finally {
                        isExecuting.set(false)
                    }
                }
                delay(period)
            }
        }
    }

    fun isScanning(): Boolean = scanState.get() == ScanState.SCANNING
    fun isPaused(): Boolean = scanState.get() == ScanState.PAUSED
    fun isStopped(): Boolean = scanState.get() == ScanState.STOPPED

    fun stopScanning() {
        println("Attempting to stop scanning... $scanState")
        if (scanState.get() == ScanState.SCANNING) {
            scanState.set(ScanState.STOPPED)
            scanningJob?.cancel()
        }
    }

    fun pauseScanning() {
        println("Attempting to pause scanning... $scanState")
        if (scanState.compareAndSet(ScanState.SCANNING, ScanState.PAUSED)) {
            scanningJob?.cancel()
        }
    }

    fun resumeScanning() {
        println("Attempting to resume scanning... $scanState")
        if (scanState.get() == ScanState.PAUSED) {
            startScanning(initialDelay, period)
        }
    }

    fun shutdown() {
        println("[$uniqueId] Shutting down scope")
        coroutineScope.cancel() // Cancel all coroutines started by this scope
    }
}