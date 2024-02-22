package com.enaboapps.switchify.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.scanning.SwitchListener
import com.enaboapps.switchify.service.utils.NodeExaminer

class SwitchifyAccessibilityService : AccessibilityService() {

    private val TAG = "SwitchifyAccessibilityService"

    private var scanningManager: ScanningManager? = null

    private var switchListener: SwitchListener? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent: ${event?.eventType}")

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                NodeExaminer.findRowsOfNodes(rootNode)
                for (row in NodeExaminer.currentRows) {
                    for (node in row) {
                        Log.d(TAG, "Node: $node")
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt")
    }

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected")
        super.onServiceConnected()

        scanningManager = ScanningManager(this, this)

        scanningManager?.setup()

        switchListener = SwitchListener(this, scanningManager!!)

        GestureManager.getInstance().setup(this)
    }


    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            return switchListener?.onSwitchPressed(event.keyCode) ?: false
        } else if (event?.action == KeyEvent.ACTION_UP) {
            return switchListener?.onSwitchReleased(event.keyCode) ?: false
        }
        return true
    }
}