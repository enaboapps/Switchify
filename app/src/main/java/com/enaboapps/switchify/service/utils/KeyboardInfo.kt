package com.enaboapps.switchify.service.utils

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo
import com.enaboapps.switchify.service.scanning.ScanReceiver

object KeyboardInfo {
    var isKeyboardVisible = false
    var keyboardHeight = 0

    // Track last scan state to go back to it after keyboard is dismissed
    var lastScanState: ScanReceiver.ReceiverState = ScanReceiver.ReceiverState.CURSOR

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
            if (ScanReceiver.state == ScanReceiver.ReceiverState.ITEM_SCAN) {
                lastScanState = ScanReceiver.state
                ScanReceiver.state = ScanReceiver.ReceiverState.CURSOR
            }
        } else {
            isKeyboardVisible = false
            keyboardHeight = 0

            // Go back to last scan state
            if (ScanReceiver.state == ScanReceiver.ReceiverState.CURSOR) {
                ScanReceiver.state = lastScanState
            }
        }
        Log.d("KeyboardInfo", "isKeyboardVisible: $isKeyboardVisible window count: ${windows.size}")
    }
}