package com.enaboapps.switchify.service.scanning

interface ScanStateInterface {
    fun pauseScanning()
    fun resumeScanning()
    fun stopScanning()
}