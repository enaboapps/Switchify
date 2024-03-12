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

/**
 * ScanningScheduler is a class that manages the scheduling of scanning tasks.
 * It uses coroutines to run the scanning tasks asynchronously.
 *
 * @property uniqueId A unique identifier for the scanning scheduler.
 * @property context The application context.
 * @property onScan A suspend function that gets executed during each scan.
 */
class ScanningScheduler(
    private var uniqueId: String,
    private val context: Context,
    private val onScan: suspend () -> Unit
) {

    /**
     * The secondary constructor for the ScanningScheduler class.
     * It generates a unique identifier for the scanning scheduler.
     *
     * @param context The application context.
     * @param onScan A suspend function that gets executed during each scan.
     */
    constructor(context: Context, onScan: suspend () -> Unit) : this(
        UUID.randomUUID().toString(),
        context,
        onScan
    )

    /**
     * The CoroutineScope in which the scanning tasks are launched.
     */
    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName(uniqueId))

    /**
     * The Job representing the currently running scanning task.
     */
    private var scanningJob: Job? = null

    /**
     * A flag indicating whether a scanning task is currently executing.
     */
    private val isExecuting = AtomicBoolean(false)

    /**
     * The initial delay before the first scanning task is launched.
     */
    private var initialDelay: Long = 0L

    /**
     * The period between successive scanning tasks.
     */
    private var period: Long = 1000L // Default period of 1 second

    /**
     * The current state of the scanner.
     */
    private var scanState = AtomicReference(ScanState.STOPPED)

    /**
     * The settings for the scanning tasks.
     */
    private val scanSettings = ScanSettings(context)

    /**
     * Starts the scanning tasks.
     *
     * @param initialDelay The initial delay before the first scanning task is launched.
     * @param period The period between successive scanning tasks.
     */
    fun startScanning(
        initialDelay: Long = scanSettings.getScanRate(),
        period: Long = scanSettings.getScanRate()
    ) {
        // If the scanner is already scanning, print a message and return
        if (scanState.get() == ScanState.SCANNING) {
            println("[$uniqueId] Already scanning")
            return
        }

        // Set the scanner state to SCANNING
        scanState.set(ScanState.SCANNING)

        // Calculate the initial delay plus the pause on the first item
        val initialDelayPlusPause = initialDelay + scanSettings.getPauseOnFirstItemDelay()

        // Set the initial delay and period
        this.initialDelay = initialDelay
        this.period = period

        // Cancel any previous scanning job
        scanningJob?.cancel()

        // Start a new scanning job
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

    /**
     * Checks if the scanner is currently scanning.
     *
     * @return True if the scanner is scanning, false otherwise.
     */
    fun isScanning(): Boolean = scanState.get() == ScanState.SCANNING

    /**
     * Checks if the scanner is currently paused.
     *
     * @return True if the scanner is paused, false otherwise.
     */
    fun isPaused(): Boolean = scanState.get() == ScanState.PAUSED

    /**
     * Checks if the scanner is currently stopped.
     *
     * @return True if the scanner is stopped, false otherwise.
     */
    fun isStopped(): Boolean = scanState.get() == ScanState.STOPPED

    /**
     * Stops the scanning tasks.
     */
    fun stopScanning() {
        println("Attempting to stop scanning... $scanState")
        if (scanState.get() == ScanState.SCANNING) {
            scanState.set(ScanState.STOPPED)
            scanningJob?.cancel()
        }
    }

    /**
     * Pauses the scanning tasks.
     */
    fun pauseScanning() {
        println("Attempting to pause scanning... $scanState")
        if (scanState.compareAndSet(ScanState.SCANNING, ScanState.PAUSED)) {
            scanningJob?.cancel()
        }
    }

    /**
     * Resumes the scanning tasks.
     */
    fun resumeScanning() {
        println("Attempting to resume scanning... $scanState")
        if (scanState.get() == ScanState.PAUSED) {
            startScanning(initialDelay, period)
        }
    }

    /**
     * Shuts down the CoroutineScope, cancelling all active coroutines.
     */
    fun shutdown() {
        println("[$uniqueId] Shutting down scope")
        coroutineScope.cancel() // Cancel all coroutines started by this scope
    }
}