package com.enaboapps.switchify

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanningSchedulerTest {
    /**
     * This function tests the scanning scheduler by rapidly starting and stopping the scanning
     */
    @Test
    fun testScanningScheduler() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scanningScheduler = ScanningScheduler(context) {
            println("Scanning...")
        }
        for (i in 0..100) {
            scanningScheduler.startScanning()
            assert(scanningScheduler.isScanning())
            Thread.sleep(100)
            scanningScheduler.stopScanning()
            Thread.sleep(100)
            assert(scanningScheduler.isStopped())
        }
    }
}