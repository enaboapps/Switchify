package com.enaboapps.switchify.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.nodes.NodeExaminer
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.selection.AutoSelectionHandler
import com.enaboapps.switchify.service.switches.SwitchListener
import com.enaboapps.switchify.service.utils.KeyboardInfo

/**
 * This is the main service class for the Switchify application.
 * It extends the AccessibilityService class to provide accessibility features.
 */
class SwitchifyAccessibilityService : AccessibilityService() {

    // ScanningManager instance for managing scanning operations
    private lateinit var scanningManager: ScanningManager

    // SwitchListener instance for listening to switch events
    private lateinit var switchListener: SwitchListener

    /**
     * This function is called when the service is destroyed.
     * It shuts down the scanning manager.
     */
    override fun onDestroy() {
        super.onDestroy()
        scanningManager.shutdown()
    }

    /**
     * This method is called when an AccessibilityEvent is fired.
     * It updates the keyboard state and finds nodes in the active window.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        rootInActiveWindow?.let { rootNode ->
            NodeExaminer.findNodes(rootNode, this)
        }
        KeyboardInfo.updateKeyboardState(windows)
    }

    /**
     * This method is called when the service is interrupted.
     * Currently, it does nothing.
     */
    override fun onInterrupt() {}

    /**
     * This method is called when the service is connected.
     * It sets up the scanning manager, switch listener, gesture manager, and auto selection handler.
     * It also finds nodes in the active window and updates the keyboard state.
     */
    override fun onServiceConnected() {
        super.onServiceConnected()

        ScanMethod.preferenceManager = PreferenceManager(this.applicationContext)

        scanningManager = ScanningManager(this, this)
        scanningManager.setup()

        switchListener = SwitchListener(this, scanningManager)

        GestureManager.getInstance().setup(this)
        AutoSelectionHandler.init(this)

        rootInActiveWindow?.let { rootNode ->
            NodeExaminer.findNodes(rootNode, this)
        }
        KeyboardInfo.updateKeyboardState(windows)
    }

    /**
     * This method is called when a key event is fired.
     * It handles switch press and release events.
     */
    override fun onKeyEvent(event: KeyEvent?): Boolean {
        return when (event?.action) {
            KeyEvent.ACTION_DOWN -> switchListener.onSwitchPressed(event.keyCode)
            KeyEvent.ACTION_UP -> switchListener.onSwitchReleased(event.keyCode)
            else -> true
        }
    }
}