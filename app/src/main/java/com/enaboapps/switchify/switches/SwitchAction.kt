package com.enaboapps.switchify.switches

import com.enaboapps.switchify.service.custom.actions.data.ActionExtra
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class SwitchAction(
    @SerializedName("id") val id: Int,
    @SerializedName("extra") val extra: ActionExtra? = null
) {
    companion object {
        fun fromJson(json: String): SwitchAction = Gson().fromJson(json, SwitchAction::class.java)

        val actions: List<SwitchAction> = listOf(
            ACTION_NONE, ACTION_SELECT, ACTION_STOP_SCANNING, ACTION_CHANGE_SCANNING_DIRECTION,
            ACTION_MOVE_TO_NEXT_ITEM, ACTION_MOVE_TO_PREVIOUS_ITEM, ACTION_TOGGLE_GESTURE_LOCK,
            ACTION_SYS_HOME, ACTION_SYS_BACK, ACTION_SYS_RECENTS, ACTION_SYS_QUICK_SETTINGS,
            ACTION_SYS_NOTIFICATIONS, ACTION_SYS_LOCK_SCREEN, ACTION_OPEN_APP
        ).map { SwitchAction(it) }

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

    fun toJson(): String = Gson().toJson(this)

    fun hasExtra(): Boolean = extra != null

    fun isExtraAvailable(): Boolean = id == ACTION_OPEN_APP

    fun getActionName(): String = when {
        hasExtra() -> getActionNameWithExtra()
        else -> getActionNameWithoutExtra()
    }

    private fun getActionNameWithExtra(): String = when (id) {
        ACTION_OPEN_APP -> "Open ${extra?.appName}"
        else -> getActionNameWithoutExtra()
    }

    private fun getActionNameWithoutExtra(): String = when (id) {
        ACTION_NONE -> "None"
        ACTION_SELECT -> "Select"
        ACTION_STOP_SCANNING -> "Stop Scanning"
        ACTION_CHANGE_SCANNING_DIRECTION -> "Change Scanning Direction"
        ACTION_MOVE_TO_NEXT_ITEM -> "Move to Next Item"
        ACTION_MOVE_TO_PREVIOUS_ITEM -> "Move to Previous Item"
        ACTION_TOGGLE_GESTURE_LOCK -> "Toggle Gesture Lock"
        ACTION_SYS_HOME -> "Home"
        ACTION_SYS_BACK -> "Back"
        ACTION_SYS_RECENTS -> "Recents"
        ACTION_SYS_QUICK_SETTINGS -> "Quick Settings"
        ACTION_SYS_NOTIFICATIONS -> "Notifications"
        ACTION_SYS_LOCK_SCREEN -> "Lock Screen"
        ACTION_OPEN_APP -> "Open App"
        else -> "Unknown"
    }

    fun getActionDescription(): String = when (id) {
        ACTION_NONE -> "Do nothing"
        ACTION_SELECT -> "Select the current item"
        ACTION_STOP_SCANNING -> "Stop scanning"
        ACTION_CHANGE_SCANNING_DIRECTION -> "Change the scanning direction"
        ACTION_MOVE_TO_NEXT_ITEM -> "Move to the next item"
        ACTION_MOVE_TO_PREVIOUS_ITEM -> "Move to the previous item"
        ACTION_TOGGLE_GESTURE_LOCK -> "Toggle the gesture lock"
        ACTION_SYS_HOME -> "Go to the home screen"
        ACTION_SYS_BACK -> "Go back"
        ACTION_SYS_RECENTS -> "Open the recent apps"
        ACTION_SYS_QUICK_SETTINGS -> "Open the quick settings"
        ACTION_SYS_NOTIFICATIONS -> "Open the notifications"
        ACTION_SYS_LOCK_SCREEN -> "Lock the screen"
        ACTION_OPEN_APP -> "Open an app"
        else -> "Unknown"
    }
}