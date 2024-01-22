package com.enaboapps.switchify.service.scanning

class ScanMode(val id: Int) {
    object Modes {
        const val MODE_AUTO = 0
        const val MODE_MANUAL = 1
    }

    // static array of modes
    companion object {
        val modes = arrayOf(
            ScanMode(Modes.MODE_AUTO),
            ScanMode(Modes.MODE_MANUAL)
        )
    }

    fun getModeName(): String {
        return when (id) {
            Modes.MODE_AUTO -> "Auto"
            Modes.MODE_MANUAL -> "Manual"
            else -> "Unknown"
        }
    }

    fun getModeDescription(): String {
        return when (id) {
            Modes.MODE_AUTO -> "Automatically scan and use a single switch to select"
            Modes.MODE_MANUAL -> "Use a switch to move between items and another switch to select"
            else -> "Unknown"
        }
    }
}