package com.enaboapps.switchify.switches

class SwitchAction(val id: Int) {
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
    }

    // static array of actions
    companion object {
        val actions = arrayOf(
            SwitchAction(Actions.ACTION_NONE),
            SwitchAction(Actions.ACTION_SELECT),
            SwitchAction(Actions.ACTION_STOP_SCANNING),
            SwitchAction(Actions.ACTION_CHANGE_SCANNING_DIRECTION),
            SwitchAction(Actions.ACTION_MOVE_TO_NEXT_ITEM),
            SwitchAction(Actions.ACTION_MOVE_TO_PREVIOUS_ITEM),
            SwitchAction(Actions.ACTION_TOGGLE_GESTURE_LOCK),
            SwitchAction(Actions.ACTION_SYS_HOME),
            SwitchAction(Actions.ACTION_SYS_BACK),
            SwitchAction(Actions.ACTION_SYS_RECENTS),
            SwitchAction(Actions.ACTION_SYS_QUICK_SETTINGS),
            SwitchAction(Actions.ACTION_SYS_NOTIFICATIONS),
            SwitchAction(Actions.ACTION_SYS_LOCK_SCREEN)
        )
    }

    fun getActionName(): String {
        return when (id) {
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
            else -> "Unknown"
        }
    }

    fun getActionDescription(): String {
        return when (id) {
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
            else -> "Unknown"
        }
    }
}