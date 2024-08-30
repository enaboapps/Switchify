package com.enaboapps.switchify.service.gestures

import com.enaboapps.switchify.service.window.ServiceMessageHUD

class GestureLockManager {
    private var isLocked = false
    private var lockedGestureData: GestureData? = null

    // Function to lock/unlock the gesture lock, showing a message to the user
    fun toggleGestureLock() {
        isLocked = !isLocked
        if (isLocked) {
            ServiceMessageHUD.instance.showMessage(
                "Gesture lock enabled. Choose a gesture to lock to your switch. You can disable it by holding your switch.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
        } else {
            ServiceMessageHUD.instance.showMessage(
                "Gesture lock disabled. Your switches will now perform their default actions.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
            setLockedGestureData(null)
        }
    }

    // Function to check if the gesture lock is enabled and if gesture data is not null
    fun isGestureLockEnabled(): Boolean {
        return isLocked && lockedGestureData != null
    }

    // Function to get the locked gesture data
    fun getLockedGestureData(): GestureData? {
        return lockedGestureData
    }

    // Function to set the locked gesture data
    fun setLockedGestureData(gestureData: GestureData?) {
        lockedGestureData = gestureData
    }

    // Helper function to get a user-friendly name for the gesture type
    private fun getGestureName(gestureType: GestureData.GestureType): String {
        return when (gestureType) {
            GestureData.GestureType.TAP -> "tap"
            GestureData.GestureType.DOUBLE_TAP -> "double tap"
            GestureData.GestureType.TAP_AND_HOLD -> "tap and hold"
            GestureData.GestureType.SWIPE -> "swipe"
            GestureData.GestureType.ZOOM -> "zoom"
            GestureData.GestureType.DRAG -> "drag" // Note: This shouldn't be used as per your requirements
        }
    }

    // Function to check if a gesture type can be locked
    fun canLockGesture(gestureType: GestureData.GestureType): Boolean {
        return gestureType != GestureData.GestureType.DRAG
    }
}