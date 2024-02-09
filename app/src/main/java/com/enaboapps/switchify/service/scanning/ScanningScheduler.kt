package com.enaboapps.switchify.service.scanning

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ScanningScheduler(private val onScan: () -> Unit) {

    private val executorService = Executors.newSingleThreadScheduledExecutor()
    private var scheduledFuture: ScheduledFuture<*>? = null
    private val isScanning = AtomicBoolean(false)

    fun startScanning(initialDelay: Long, period: Long) {
        // Ensure any ongoing scan is cancelled before starting a new one
        stopScanning()

        isScanning.set(true)
        scheduledFuture = executorService.scheduleAtFixedRate({
            // Check if the task is still supposed to run
            if (isScanning.get()) {
                onScan()
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS)
    }

    fun stopScanning() {
        // Attempt to cancel the future if it's running
        scheduledFuture?.cancel(true)
        isScanning.set(false)
    }

    fun shutdown() {
        // Shutdown the executor service when it's no longer needed
        executorService.shutdownNow()
        try {
            // Wait a while for existing tasks to terminate
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow() // Cancel currently executing tasks
            }
        } catch (ie: InterruptedException) {
            // Preserve interrupt status
            Thread.currentThread().interrupt()
        }
    }
}