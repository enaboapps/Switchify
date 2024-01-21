package com.enaboapps.switchify.service.gestures

import android.content.Context
import android.widget.Toast

class SwipeLockManager(private val context: Context) {
    var isLocked = false
    var swipeLockDirection: GestureManager.SwipeDirection? = null

    // Function to lock/unlock the swipe lock, showing a toast message to the user
    fun toggleSwipeLock() {
        isLocked = !isLocked
        if (isLocked) {
            Toast.makeText(context, "Swipe lock enabled. Choose a direction. Hold a switch to disable.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Swipe lock disabled.", Toast.LENGTH_SHORT).show()
            swipeLockDirection = null
        }
    }

    // Function to check if the swipe lock is enabled and if direction is not null
    fun isSwipeLockEnabled(): Boolean {
        return isLocked && swipeLockDirection != null
    }
}