package com.enaboapps.switchify.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class SwitchifyAccessibilityService : AccessibilityService() {

    private val TAG = "SwitchifyAccessibilityService"

    private val cursorManager: CursorManager = CursorManager(this)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent: ${event?.eventType}")
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected")
        super.onServiceConnected()

        cursorManager.setupLayout()
        cursorManager.start()
    }


    override fun onKeyEvent(event: KeyEvent?): Boolean {
        cursorManager.performAction()
        return true
    }

}