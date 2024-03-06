package com.enaboapps.switchify.service.utils

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo
import com.enaboapps.switchify.service.scanning.ScanReceiver

object KeyboardInfo {
    var isKeyboardVisible = false
    var keyboardHeight = 0

    // Track scan receiver state to go back to when keyboard is hidden
    private var previousScanReceiverState = ScanReceiver.state

    fun updateKeyboardState(windows: List<AccessibilityWindowInfo>) {
        val keyboardWindow = windows.firstOrNull { window ->
            window.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }
        if (keyboardWindow != null) {
            isKeyboardVisible = true
            val rect = Rect()
            keyboardWindow.getBoundsInScreen(rect)
            keyboardHeight = rect.height()

            // Go to cursor as keyboard keys don't report AccessibilityNodeInfo
            previousScanReceiverState = ScanReceiver.state
            ScanReceiver.state = ScanReceiver.ReceiverState.CURSOR
        } else {
            isKeyboardVisible = false
            keyboardHeight = 0

            // Go back to previous state
            ScanReceiver.state = previousScanReceiverState
        }
        Log.d("KeyboardInfo", "isKeyboardVisible: $isKeyboardVisible window count: ${windows.size}")
    }
}