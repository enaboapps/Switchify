package com.enaboapps.switchify.service.scanning

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import android.content.Context
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.nodes.NodeScanner
import com.enaboapps.switchify.service.nodes.NodeScannerUI
import com.enaboapps.switchify.service.radar.RadarManager
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import com.enaboapps.switchify.switches.SwitchAction

/**
 * ScanningManager is responsible for managing the scanning process in the application.
 * It sets up and controls the scanning methods, performs actions and manages the scanning state.
 *
 * @property accessibilityService the accessibility service instance.
 * @property context the application context.
 */
class ScanningManager(
    private val accessibilityService: SwitchifyAccessibilityService,
    val context: Context
) {

    // cursor manager
    private val cursorManager = CursorManager(context)

    // radar manager
    private val radarManager = RadarManager(context)

    // node scanner
    private val nodeScanner = NodeScanner()

    /**
     * This function sets up the scanning manager.
     * It initializes the accessibility window, menu manager and scanning methods.
     */
    fun setup() {
        SwitchifyAccessibilityWindow.instance.setup(context)
        SwitchifyAccessibilityWindow.instance.show()

        nodeScanner.start(context)
        nodeScanner.registerEventReceivers(context)

        MenuManager.getInstance().setup(this, accessibilityService)
    }

    /**
     * This function explicitly sets the type of the scanning manager to cursor.
     */
    fun setCursorType() {
        ScanMethod.setType(ScanMethod.MethodType.CURSOR)
        ScanMethod.isInMenu = false

        NodeScannerUI.instance.hideAll()
    }

    /**
     * This function explicitly sets the type of the scanning manager to radar.
     */
    fun setRadarType() {
        ScanMethod.setType(ScanMethod.MethodType.RADAR)
        ScanMethod.isInMenu = false

        NodeScannerUI.instance.hideAll()
    }

    /**
     * This function explicitly sets the type of the scanning manager to item scan.
     * It also starts the NodeScanner timeout.
     */
    fun setItemScanType() {
        ScanMethod.setType(ScanMethod.MethodType.ITEM_SCAN)
        ScanMethod.isInMenu = false

        // Start the NodeScanner timeout
        nodeScanner.startTimeoutToRevertToCursor()
    }

    /**
     * This function explicitly sets the type of the scanning manager to menu.
     */
    fun setMenuType() {
        ScanMethod.isInMenu = true
    }

    /**
     * This function makes a selection.
     * It checks the current scanning method type and performs the corresponding selection action.
     */
    fun select() {
        if (ScanMethod.isInMenu) {
            // Select the menu item
            MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.performSelection()
            return
        }

        when (ScanMethod.getType()) {
            ScanMethod.MethodType.CURSOR -> {
                // Perform the cursor action
                cursorManager.performSelectionAction()
            }

            ScanMethod.MethodType.RADAR -> {
                // Perform the radar action
                radarManager.performSelectionAction()
            }

            ScanMethod.MethodType.ITEM_SCAN -> {
                // Perform the item scan action
                nodeScanner.scanTree.performSelection()
            }
        }
    }

    /**
     * This function performs an action.
     * It checks the action id and performs the corresponding action.
     *
     * @param action the action to be performed.
     */
    fun performAction(action: SwitchAction) {
        cleanupInactiveScanningMethods()

        // If the gesture lock is enabled, perform the gesture lock action
        if (GestureManager.getInstance().performGestureLockAction()) {
            return
        }

        // Perform the action based on the action id
        when (action.id) {
            SwitchAction.Actions.ACTION_NONE -> {
                // do nothing
            }

            SwitchAction.Actions.ACTION_SELECT -> {
                select()
            }

            SwitchAction.Actions.ACTION_STOP_SCANNING -> {
                if (ScanMethod.isInMenu) {
                    // Stop the menu scanning
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stopScanning()
                    return
                }

                when (ScanMethod.getType()) {
                    ScanMethod.MethodType.CURSOR -> {
                        // reset the cursor
                        cursorManager.externalReset()
                    }

                    ScanMethod.MethodType.RADAR -> {
                        // reset the radar
                        radarManager.resetRadar()
                    }

                    ScanMethod.MethodType.ITEM_SCAN -> {
                        // Stop item scanning
                        nodeScanner.scanTree.stopScanning()
                    }
                }
            }

            SwitchAction.Actions.ACTION_CHANGE_SCANNING_DIRECTION -> {
                if (ScanMethod.isInMenu) {
                    // Change the menu scanning direction
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.swapScanDirection()
                    return
                }

                when (ScanMethod.getType()) {
                    ScanMethod.MethodType.CURSOR -> {
                        // Change the cursor direction
                        cursorManager.swapDirection()
                    }

                    ScanMethod.MethodType.RADAR -> {
                        // Change the radar direction
                        radarManager.toggleDirection()
                    }

                    ScanMethod.MethodType.ITEM_SCAN -> {
                        // Change the item scan direction
                        nodeScanner.scanTree.swapScanDirection()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_NEXT_ITEM -> {
                if (ScanMethod.isInMenu) {
                    // Move the menu to the next item
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepForward()
                    return
                }

                when (ScanMethod.getType()) {
                    ScanMethod.MethodType.CURSOR -> {
                        // Move the cursor to the next item
                        cursorManager.moveToNextItem()
                    }

                    ScanMethod.MethodType.ITEM_SCAN -> {
                        // Move to the next item
                        nodeScanner.scanTree.stepForward()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_PREVIOUS_ITEM -> {
                if (ScanMethod.isInMenu) {
                    // Move the menu to the previous item
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepBackward()
                    return
                }

                when (ScanMethod.getType()) {
                    ScanMethod.MethodType.CURSOR -> {
                        // Move the cursor to the previous item
                        cursorManager.moveToPreviousItem()
                    }

                    ScanMethod.MethodType.ITEM_SCAN -> {
                        // Move to the previous item
                        nodeScanner.scanTree.stepBackward()
                    }
                }
            }

            SwitchAction.Actions.ACTION_TOGGLE_GESTURE_LOCK -> {
                // Toggle the gesture lock
                GestureManager.getInstance().toggleGestureLock()
            }

            SwitchAction.Actions.ACTION_SYS_HOME -> {
                // Go to the home screen
                accessibilityService.performGlobalAction(GLOBAL_ACTION_HOME)
            }

            SwitchAction.Actions.ACTION_SYS_BACK -> {
                // Go back
                accessibilityService.performGlobalAction(GLOBAL_ACTION_BACK)
            }

            SwitchAction.Actions.ACTION_SYS_RECENTS -> {
                // Open the recent apps
                accessibilityService.performGlobalAction(GLOBAL_ACTION_RECENTS)
            }

            SwitchAction.Actions.ACTION_SYS_QUICK_SETTINGS -> {
                // Open the quick settings
                accessibilityService.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
            }

            SwitchAction.Actions.ACTION_SYS_NOTIFICATIONS -> {
                // Open the notifications
                accessibilityService.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
            }

            SwitchAction.Actions.ACTION_SYS_LOCK_SCREEN -> {
                // Lock the screen
                accessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
        }
    }

    /**
     * This function pauses the scanning.
     * It checks the current scanning method type and pauses the corresponding scanning process.
     */
    fun pauseScanning() {
        if (ScanMethod.isInMenu) {
            // Pause the menu
            MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.pauseScanning()
            return
        }

        when (ScanMethod.getType()) {
            ScanMethod.MethodType.CURSOR -> {
                // Pause the cursor
                cursorManager.pauseScanning()
            }

            ScanMethod.MethodType.RADAR -> {
                // Pause the radar
                radarManager.pauseScanning()
            }

            ScanMethod.MethodType.ITEM_SCAN -> {
                // Pause the item scan
                nodeScanner.scanTree.pauseScanning()
            }
        }
    }

    /**
     * This function resumes the scanning.
     * It checks the current scanning method type and resumes the corresponding scanning process.
     */
    fun resumeScanning() {
        if (ScanMethod.isInMenu) {
            // Resume the menu
            MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.resumeScanning()
            return
        }

        when (ScanMethod.getType()) {
            ScanMethod.MethodType.CURSOR -> {
                // Resume the cursor
                cursorManager.resumeScanning()
            }

            ScanMethod.MethodType.RADAR -> {
                // Resume the radar
                radarManager.resumeScanning()
            }

            ScanMethod.MethodType.ITEM_SCAN -> {
                // Resume the item scan
                nodeScanner.scanTree.resumeScanning()
            }
        }
    }

    /**
     * This function cleans up the inactive scanning methods.
     * It checks the current scanning method type and cleans up the other scanning methods.
     */
    private fun cleanupInactiveScanningMethods() {
        when (ScanMethod.getType()) {
            ScanMethod.MethodType.CURSOR -> {
                // Clean up the radar and item scan
                radarManager.cleanup()
                nodeScanner.cleanup()
            }

            ScanMethod.MethodType.RADAR -> {
                // Clean up the cursor and item scan
                cursorManager.cleanup()
                nodeScanner.cleanup()
            }

            ScanMethod.MethodType.ITEM_SCAN -> {
                // Clean up the cursor and radar
                cursorManager.cleanup()
                radarManager.cleanup()
            }
        }
    }

    /**
     * This function resets the scanning manager.
     * It stops the scanning and cleans up the resources.
     * It also closes the menu.
     */
    fun reset() {
        // Stop scanning
        pauseScanning()

        // Clean up resources
        cursorManager.cleanup()
        nodeScanner.cleanup()
        radarManager.cleanup()

        // Close the menu
        MenuManager.getInstance().closeMenuHierarchy()
    }

    /**
     * This function shuts down the scanning manager.
     * It stops the scanning and cleans up the resources.
     */
    fun shutdown() {
        // Stop scanning
        pauseScanning()

        // Clean up resources
        cursorManager.cleanup()
        nodeScanner.cleanup()
    }
}