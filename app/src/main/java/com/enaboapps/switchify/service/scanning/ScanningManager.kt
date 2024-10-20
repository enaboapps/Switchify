package com.enaboapps.switchify.service.scanning

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.custom.actions.ActionPerformer
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.methods.cursor.CursorManager
import com.enaboapps.switchify.service.methods.nodes.Node
import com.enaboapps.switchify.service.methods.nodes.NodeScanner
import com.enaboapps.switchify.service.methods.nodes.NodeScannerUI
import com.enaboapps.switchify.service.methods.radar.RadarManager
import com.enaboapps.switchify.service.selection.SelectionHandler
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import com.enaboapps.switchify.switches.SwitchAction

/**
 * ScanningManager is responsible for managing the scanning process in the application.
 * It coordinates different scanning methods (cursor, radar, item scan) and handles user actions.
 *
 * @property accessibilityService The accessibility service instance used for system-level actions.
 * @property context The application context.
 */
class ScanningManager(
    private val accessibilityService: SwitchifyAccessibilityService,
    val context: Context
) : ScanMethodObserver {
    // Managers for different scanning methods
    private val cursorManager = CursorManager(context)
    private val radarManager = RadarManager(context)
    private val nodeScanner = NodeScanner()

    // Scan settings
    private val scanSettings = ScanSettings(context)

    /**
     * Provides the current active scanning state method on the current scanning method.
     */
    private val currentScanMethod: ScanMethodBase
        get() = when {
            ScanMethod.isInMenu -> MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree
            else -> when (ScanMethod.getType()) {
                ScanMethod.MethodType.CURSOR -> cursorManager
                ScanMethod.MethodType.RADAR -> radarManager
                ScanMethod.MethodType.ITEM_SCAN -> nodeScanner.scanTree
                else -> {
                    throw IllegalStateException("Invalid scanning method type: ${ScanMethod.getType()}")
                }
            }
        } as ScanMethodBase

    override fun onScanMethodChanged(type: String) {
        cleanupInactiveScanningMethods(type)
    }

    override fun onMenuStateChanged(isInMenu: Boolean) {
        cleanupInactiveScanningMethods(ScanMethod.getType())

        // Start scanning if the setting is enabled after 600ms
        Handler(Looper.getMainLooper()).postDelayed({
            if (scanSettings.getAutomaticallyStartScanAfterSelection()) {
                currentScanMethod.startScanning()
            }
        }, 600)
    }

    /**
     * Sets up the scanning manager, initializing necessary components.
     */
    fun setup() {
        SwitchifyAccessibilityWindow.instance.setup(context)
        SwitchifyAccessibilityWindow.instance.show()
        nodeScanner.start(context)
        MenuManager.getInstance().setup(this, accessibilityService)
        ScanMethod.observer = this
    }

    /**
     * Updates the nodes in the NodeScanner with the current layout information.
     *
     * @param nodes List of Node instances representing the current screen layout.
     */
    fun updateNodes(nodes: List<Node>) {
        nodeScanner.setScreenNodes(nodes)
    }

    /**
     * Sets the scanning method to cursor type.
     */
    fun setCursorType() {
        setType(ScanMethod.MethodType.CURSOR)
    }

    /**
     * Sets the scanning method to radar type.
     */
    fun setRadarType() {
        setType(ScanMethod.MethodType.RADAR)
    }

    /**
     * Sets the scanning method to item scan type and starts the timeout to revert to cursor.
     */
    fun setItemScanType() {
        setType(ScanMethod.MethodType.ITEM_SCAN)
        SelectionHandler.setStartScanningAction { nodeScanner.scanTree.startScanning() }
        nodeScanner.startTimeoutToRevertToCursor()
    }

    /**
     * Sets the scanning method to menu type.
     */
    fun setMenuType() {
        ScanMethod.isInMenu = true
        NodeScannerUI.instance.hideAll()
    }

    /**
     * Helper method to set the scanning method type and perform necessary cleanup.
     *
     * @param type The ScanMethod.MethodType to set. Must be a valid type.
     */
    private fun setType(type: String) {
        ScanMethod.setType(type)
        ScanMethod.isInMenu = false
        NodeScannerUI.instance.hideAll()
        nodeScanner.scanTree.reset()
    }

    /**
     * Performs the selection action for the current scanning state.
     */
    fun select() {
        currentScanMethod.performSelectionAction()
    }

    /**
     * Performs the specified action based on the SwitchAction type.
     *
     * @param action The SwitchAction to perform.
     */
    fun performAction(action: SwitchAction) {
        cleanupInactiveScanningMethods(ScanMethod.getType())

        if (GestureManager.getInstance().performGestureLockAction()) {
            return
        }

        when (action.id) {
            SwitchAction.ACTION_SELECT -> select()
            SwitchAction.ACTION_STOP_SCANNING -> currentScanMethod.stopScanning()
            SwitchAction.ACTION_CHANGE_SCANNING_DIRECTION -> currentScanMethod.swapScanDirection()
            SwitchAction.ACTION_MOVE_TO_NEXT_ITEM -> currentScanMethod.stepForward()
            SwitchAction.ACTION_MOVE_TO_PREVIOUS_ITEM -> currentScanMethod.stepBackward()
            SwitchAction.ACTION_TOGGLE_GESTURE_LOCK -> GestureManager.getInstance()
                .toggleGestureLock()

            SwitchAction.ACTION_SYS_HOME -> accessibilityService.performGlobalAction(
                GLOBAL_ACTION_HOME
            )

            SwitchAction.ACTION_SYS_BACK -> accessibilityService.performGlobalAction(
                GLOBAL_ACTION_BACK
            )

            SwitchAction.ACTION_SYS_RECENTS -> accessibilityService.performGlobalAction(
                GLOBAL_ACTION_RECENTS
            )

            SwitchAction.ACTION_SYS_QUICK_SETTINGS -> accessibilityService.performGlobalAction(
                GLOBAL_ACTION_QUICK_SETTINGS
            )

            SwitchAction.ACTION_SYS_NOTIFICATIONS -> accessibilityService.performGlobalAction(
                GLOBAL_ACTION_NOTIFICATIONS
            )

            SwitchAction.ACTION_SYS_LOCK_SCREEN -> accessibilityService.performGlobalAction(
                GLOBAL_ACTION_LOCK_SCREEN
            )

            SwitchAction.ACTION_PERFORM_USER_ACTION -> {
                val actionPerformer = ActionPerformer(context)
                actionPerformer.performAction(action.extra?.myActionsId ?: "")
            }

            else -> {} // Do nothing for ACTION_NONE
        }
    }

    /**
     * Pauses the scanning process for the current scanning state.
     */
    fun pauseScanning() {
        currentScanMethod.pauseScanning()
    }

    /**
     * Resumes the scanning process for the current scanning state.
     */
    fun resumeScanning() {
        currentScanMethod.resumeScanning()
    }

    /**
     * Cleans up inactive scanning methods to free up resources.
     */
    private fun cleanupInactiveScanningMethods(activeType: String) {
        when (activeType) {
            ScanMethod.MethodType.CURSOR -> {
                radarManager.cleanup()
                nodeScanner.scanTree.cleanup()
            }

            ScanMethod.MethodType.RADAR -> {
                cursorManager.cleanup()
                nodeScanner.scanTree.cleanup()
            }

            ScanMethod.MethodType.ITEM_SCAN -> {
                cursorManager.cleanup()
                radarManager.cleanup()
            }
        }

        NodeScannerUI.instance.hideAll()
    }

    /**
     * Resets the scanning manager, stopping all scanning processes and cleaning up resources.
     */
    fun reset() {
        pauseScanning()
        listOf(cursorManager, radarManager, nodeScanner.scanTree).forEach { it.cleanup() }
        MenuManager.getInstance().closeMenuHierarchy()
    }

    /**
     * Shuts down the scanning manager, stopping all processes and cleaning up resources.
     */
    fun shutdown() {
        pauseScanning()
        listOf(cursorManager, radarManager, nodeScanner.scanTree).forEach { it.cleanup() }
    }
}