package com.enaboapps.switchify.service.gestures

import com.enaboapps.switchify.service.scanning.ScanMethod
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
        }
    }

    // Function to check if the gesture lock is enabled and the user is not in the menu
    fun isGestureLockEnabled(): Boolean {
        return isLocked && !ScanMethod.isInMenu
    }

    // Function to get the locked gesture data
    fun getLockedGestureData(): GestureData? {
        return lockedGestureData
    }

    // Function to set the locked gesture data
    fun setLockedGestureData(gestureData: GestureData?) {
        lockedGestureData = if (gestureData != null && canLockGesture(gestureData.gestureType)) {
            gestureData
        } else {
            null
        }
    }

    // Function to check if a gesture type can be locked
    fun canLockGesture(gestureType: GestureData.GestureType): Boolean {
        val isDragOrCustomSwipe =
            gestureType == GestureData.GestureType.DRAG || gestureType == GestureData.GestureType.CUSTOM_SWIPE
        if (isLocked && isDragOrCustomSwipe) {
            ServiceMessageHUD.instance.showMessage(
                "You can only lock tap, double tap, tap and hold, swipe, and zoom gestures.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
            isLocked = false // Disable the gesture lock
            return false
        }
        return true
    }
}