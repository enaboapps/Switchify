package com.enaboapps.switchify.service.utils

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo
import com.enaboapps.switchify.service.scanning.ScanReceiver

object KeyboardInfo {
    var isKeyboardVisible = false
    var keyboardHeight = 0

    // Track last scan state to go back to it after keyboard is dismissed
    private var lastScanState: ScanReceiver.ReceiverState = ScanReceiver.state

    // Track last update time to prevent multiple updates in a short time
    private var lastUpdateTime: Long = 0

    fun updateKeyboardState(windows: List<AccessibilityWindowInfo>) {
        if (System.currentTimeMillis() - lastUpdateTime < 50) {
            return
        }
        lastUpdateTime = System.currentTimeMillis()
        val keyboardWindow = windows.firstOrNull { window ->
            window.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }
        if (keyboardWindow != null) {
            if (!isKeyboardVisible) {
                // Go to cursor as keyboard keys don't report AccessibilityNodeInfo
                if (ScanReceiver.state == ScanReceiver.ReceiverState.ITEM_SCAN) {
                    lastScanState = ScanReceiver.ReceiverState.ITEM_SCAN
                    ScanReceiver.state = ScanReceiver.ReceiverState.CURSOR
                } else {
                    lastScanState = ScanReceiver.state
                }
            }

            isKeyboardVisible = true
            val rect = Rect()
            keyboardWindow.getBoundsInScreen(rect)
            keyboardHeight = rect.height()
        } else {
            if (isKeyboardVisible) {
                // Go back to last scan state
                ScanReceiver.state = lastScanState
            }

            isKeyboardVisible = false
            keyboardHeight = 0
        }
        Log.d("KeyboardInfo", "isKeyboardVisible: $isKeyboardVisible window count: ${windows.size}")
    }
}