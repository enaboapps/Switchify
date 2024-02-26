package com.enaboapps.switchify.service.utils

import android.util.Log
import android.view.accessibility.AccessibilityWindowInfo

object KeyboardInfo {
    var isKeyboardVisible = false

    fun updateKeyboardState(windows: List<AccessibilityWindowInfo>) {
        isKeyboardVisible = windows.any { window ->
            window.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD
        }
        Log.d("KeyboardInfo", "isKeyboardVisible: $isKeyboardVisible window count: ${windows.size}")
    }
}