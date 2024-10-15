package com.enaboapps.switchify.service.scanning

interface ScanMethodBase {
    fun startScanning()
    fun pauseScanning()
    fun resumeScanning()
    fun stopScanning()
    fun swapScanDirection()
    fun stepForward()
    fun stepBackward()
    fun performSelectionAction()
    fun cleanup()
}