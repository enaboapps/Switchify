package com.enaboapps.switchify.service.utils

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo
import com.enaboapps.switchify.service.scanning.ScanMethod

object KeyboardInfo {
    var isKeyboardVisible = false
    var keyboardHeight = 0

    // Track last scan type to go back to it after keyboard is dismissed
    private var lastScanType: String = ScanMethod.getType()

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
                if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN) {
                    lastScanType = ScanMethod.MethodType.ITEM_SCAN
                    ScanMethod.setType(ScanMethod.MethodType.CURSOR)
                } else {
                    lastScanType = ScanMethod.getType()
                }
            }

            isKeyboardVisible = true
            val rect = Rect()
            keyboardWindow.getBoundsInScreen(rect)
            keyboardHeight = rect.height()
        } else {
            if (isKeyboardVisible) {
                // Go back to last scan type
                ScanMethod.setType(lastScanType)
            }

            isKeyboardVisible = false
            keyboardHeight = 0
        }
        Log.d("KeyboardInfo", "isKeyboardVisible: $isKeyboardVisible window count: ${windows.size}")
    }
}