package com.enaboapps.switchify.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.PointF
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.enaboapps.switchify.service.utils.GestureUtils

class SwitchifyAccessibilityService : AccessibilityService(), TapGestureListener {

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

        cursorManager.setup()
        cursorManager.tapGestureListener = this
    }


    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_UP) {
            cursorManager.performAction()
        }
        return true
    }



    override fun onTap(point: PointF) {
        try {
            dispatchGesture(GestureUtils().createTap(point), object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    Log.d(TAG, "onCompleted")
                }
            }, null)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}