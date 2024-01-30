package com.enaboapps.switchify.service.gestures

import com.enaboapps.switchify.service.window.ServiceMessageHUD

class SwipeLockManager {
    var isLocked = false
    var swipeLockDirection: GestureManager.SwipeDirection? = null

    // Function to lock/unlock the swipe lock, showing a message to the user
    fun toggleSwipeLock() {
        isLocked = !isLocked
        if (isLocked) {
            ServiceMessageHUD.instance.showMessage(
                "Entered swipe lock. Choose a direction and your switch will swipe when pressed. Press and hold to exit swipe lock.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
        } else {
            ServiceMessageHUD.instance.showMessage(
                "Exited swipe lock.",
                ServiceMessageHUD.MessageType.DISAPPEARING
            )
            swipeLockDirection = null
        }
    }

    // Function to check if the swipe lock is enabled and if direction is not null
    fun isSwipeLockEnabled(): Boolean {
        return isLocked && swipeLockDirection != null
    }
}