package com.enaboapps.switchify.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.scanning.SwitchListener

class SwitchifyAccessibilityService : AccessibilityService(),
    ScreenSwitchListener {

    private val TAG = "SwitchifyAccessibilityService"

    private var scanningManager: ScanningManager? = null

    private var switchListener: SwitchListener? = null

    private val screenSwitch: ScreenSwitch = ScreenSwitch(this)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "onAccessibilityEvent: ${event?.eventType}")
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

        GestureManager.getInstance().accessibilityService = this

        screenSwitch.setup()
        screenSwitch.screenSwitchListener = this
    }


    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action == KeyEvent.ACTION_DOWN) {
            switchListener?.onSwitchPressed(event.keyCode)
        } else if (event?.action == KeyEvent.ACTION_UP) {
            switchListener?.onSwitchReleased(event.keyCode)
        }
        return true
    }


    override fun onScreenSwitch() {
        scanningManager?.select()

        screenSwitch.teardown()
        screenSwitch.setup()
    }
}