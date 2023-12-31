package com.enaboapps.switchify.switches

class SwitchAction(val id: Int) {
    object Actions {
        const val ACTION_SELECT = 0
        const val ACTION_STOP_SCANNING = 1
    }

    // static array of actions
    companion object {
        val actions = arrayOf(
            SwitchAction(Actions.ACTION_SELECT),
            SwitchAction(Actions.ACTION_STOP_SCANNING)
        )
    }

    fun getActionName(): String {
        return when (id) {
            Actions.ACTION_SELECT -> "Select"
            Actions.ACTION_STOP_SCANNING -> "Stop Scanning"
            else -> "Unknown"
        }
    }
}