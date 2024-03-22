package com.enaboapps.switchify.service.utils

import android.content.Context
import android.graphics.Rect
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo
import com.enaboapps.switchify.service.scanning.ScanMethod

object KeyboardBridge {
    var isKeyboardVisible = false
    var keyboardHeight = 0

    // Track last scan type to go back to it after keyboard is dismissed
    private var lastScanType: String = ScanMethod.getType()

    fun isOurKeyboardActive(context: Context): Boolean {
        val current = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return current.contains("com.enaboapps.switchify")
    }

    fun updateKeyboardState(windows: List<AccessibilityWindowInfo>, context: Context) {
        val keyboardWindow = windows.firstOrNull { window ->
            window.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }
        if (keyboardWindow != null) {
            if (!isKeyboardVisible) {
                // Check if our keyboard is active
                if (isOurKeyboardActive(context)) {
                    // Set scan method to ITEM_SCAN
                    ScanMethod.setType(ScanMethod.MethodType.ITEM_SCAN)
                } else {
                    // Go to cursor as keyboard keys don't report AccessibilityNodeInfo
                    if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN) {
                        lastScanType = ScanMethod.MethodType.ITEM_SCAN
                        ScanMethod.setType(ScanMethod.MethodType.CURSOR)
                    } else {
                        lastScanType = ScanMethod.getType()
                    }
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
        Log.d(
            "KeyboardBridge",
            "isKeyboardVisible: $isKeyboardVisible window count: ${windows.size}"
        )
    }
}