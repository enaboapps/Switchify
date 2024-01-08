package com.enaboapps.switchify.switches

class SwitchAction(val id: Int) {
    object Actions {
        const val ACTION_NONE = 0
        const val ACTION_SELECT = 1
        const val ACTION_STOP_SCANNING = 2
        const val ACTION_CHANGE_SCANNING_DIRECTION = 3
    }

    // static array of actions
    companion object {
        val actions = arrayOf(
            SwitchAction(Actions.ACTION_NONE),
            SwitchAction(Actions.ACTION_SELECT),
            SwitchAction(Actions.ACTION_STOP_SCANNING),
            SwitchAction(Actions.ACTION_CHANGE_SCANNING_DIRECTION)
        )
    }

    fun getActionName(): String {
        return when (id) {
            Actions.ACTION_NONE -> "None"
            Actions.ACTION_SELECT -> "Select"
            Actions.ACTION_STOP_SCANNING -> "Stop Scanning"
            Actions.ACTION_CHANGE_SCANNING_DIRECTION -> "Change Scanning Direction"
            else -> "Unknown"
        }
    }
}