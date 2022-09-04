package com.enaboapps.switchify.switches

class SwitchAction(val id: Int) {
    object Actions {
        const val ACTION_SELECT = 0
        const val ACTION_STOP_SCANNING = 1
    }

    fun getActionName(): String {
        return when (id) {
            Actions.ACTION_SELECT -> "Select"
            Actions.ACTION_STOP_SCANNING -> "Stop Scanning"
            else -> "Unknown"
        }
    }
}