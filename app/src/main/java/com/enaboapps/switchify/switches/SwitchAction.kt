package com.enaboapps.switchify.switches

data class SwitchActionExtra(
    val appPackage: String = "",
    val appName: String = ""
) {
    companion object {
        fun fromString(string: String): SwitchActionExtra {
            val (appPackage, appName) = string.split(", ", limit = 2)
            return SwitchActionExtra(appPackage, appName)
        }

        fun asString(extra: SwitchActionExtra): String = "${extra.appPackage}, ${extra.appName}"
    }
}

class SwitchAction(val id: Int, val extra: SwitchActionExtra? = null) {
    companion object {
        fun fromString(string: String): SwitchAction {
            val parts = string.split(", ", limit = 2)
            val id = parts[0].toInt()
            val extra = parts.getOrNull(1)?.let { SwitchActionExtra.fromString(it) }
            return SwitchAction(id, extra)
        }

        fun asString(action: SwitchAction): String =
            "${action.id}${action.extra?.let { ", ${SwitchActionExtra.asString(it)}" } ?: ""}"

        object Actions {
            const val ACTION_NONE = 0
            const val ACTION_SELECT = 1
            const val ACTION_STOP_SCANNING = 2
            const val ACTION_CHANGE_SCANNING_DIRECTION = 3
            const val ACTION_MOVE_TO_NEXT_ITEM = 4
            const val ACTION_MOVE_TO_PREVIOUS_ITEM = 5
            const val ACTION_TOGGLE_GESTURE_LOCK = 6
            const val ACTION_SYS_HOME = 7
            const val ACTION_SYS_BACK = 8
            const val ACTION_SYS_RECENTS = 9
            const val ACTION_SYS_QUICK_SETTINGS = 10
            const val ACTION_SYS_NOTIFICATIONS = 11
            const val ACTION_SYS_LOCK_SCREEN = 12
            const val ACTION_OPEN_APP = 13
        }

        val actions = Actions.run {
            arrayOf(
                ACTION_NONE, ACTION_SELECT, ACTION_STOP_SCANNING, ACTION_CHANGE_SCANNING_DIRECTION,
                ACTION_MOVE_TO_NEXT_ITEM, ACTION_MOVE_TO_PREVIOUS_ITEM, ACTION_TOGGLE_GESTURE_LOCK,
                ACTION_SYS_HOME, ACTION_SYS_BACK, ACTION_SYS_RECENTS, ACTION_SYS_QUICK_SETTINGS,
                ACTION_SYS_NOTIFICATIONS, ACTION_SYS_LOCK_SCREEN, ACTION_OPEN_APP
            ).map(::SwitchAction)
        }
    }

    fun hasExtra(): Boolean = extra != null

    fun getActionName(): String = when {
        hasExtra() -> getActionNameWithExtra()
        else -> getActionNameWithoutExtra()
    }

    private fun getActionNameWithExtra(): String = when (id) {
        Actions.ACTION_OPEN_APP -> "Open ${extra?.appName}"
        else -> getActionNameWithoutExtra()
    }

    private fun getActionNameWithoutExtra(): String = when (id) {
        Actions.ACTION_NONE -> "None"
        Actions.ACTION_SELECT -> "Select"
        Actions.ACTION_STOP_SCANNING -> "Stop Scanning"
        Actions.ACTION_CHANGE_SCANNING_DIRECTION -> "Change Scanning Direction"
        Actions.ACTION_MOVE_TO_NEXT_ITEM -> "Move to Next Item"
        Actions.ACTION_MOVE_TO_PREVIOUS_ITEM -> "Move to Previous Item"
        Actions.ACTION_TOGGLE_GESTURE_LOCK -> "Toggle Gesture Lock"
        Actions.ACTION_SYS_HOME -> "Home"
        Actions.ACTION_SYS_BACK -> "Back"
        Actions.ACTION_SYS_RECENTS -> "Recents"
        Actions.ACTION_SYS_QUICK_SETTINGS -> "Quick Settings"
        Actions.ACTION_SYS_NOTIFICATIONS -> "Notifications"
        Actions.ACTION_SYS_LOCK_SCREEN -> "Lock Screen"
        Actions.ACTION_OPEN_APP -> "Open App"
        else -> "Unknown"
    }

    fun getActionDescription(): String = when (id) {
        Actions.ACTION_NONE -> "Do nothing"
        Actions.ACTION_SELECT -> "Select the current item"
        Actions.ACTION_STOP_SCANNING -> "Stop scanning"
        Actions.ACTION_CHANGE_SCANNING_DIRECTION -> "Change the scanning direction"
        Actions.ACTION_MOVE_TO_NEXT_ITEM -> "Move to the next item"
        Actions.ACTION_MOVE_TO_PREVIOUS_ITEM -> "Move to the previous item"
        Actions.ACTION_TOGGLE_GESTURE_LOCK -> "Toggle the gesture lock"
        Actions.ACTION_SYS_HOME -> "Go to the home screen"
        Actions.ACTION_SYS_BACK -> "Go back"
        Actions.ACTION_SYS_RECENTS -> "Open the recent apps"
        Actions.ACTION_SYS_QUICK_SETTINGS -> "Open the quick settings"
        Actions.ACTION_SYS_NOTIFICATIONS -> "Open the notifications"
        Actions.ACTION_SYS_LOCK_SCREEN -> "Lock the screen"
        Actions.ACTION_OPEN_APP -> "Open an app"
        else -> "Unknown"
    }
}