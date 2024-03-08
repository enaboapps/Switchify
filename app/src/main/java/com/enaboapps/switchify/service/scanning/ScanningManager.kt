package com.enaboapps.switchify.service.scanning

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.content.Context
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.nodes.NodeScanner
import com.enaboapps.switchify.switches.SwitchAction

class ScanningManager(
    private val accessibilityService: SwitchifyAccessibilityService,
    val context: Context
) {

    // cursor manager
    private val cursorManager = CursorManager(context)

    // node scanner
    private val nodeScanner = NodeScanner.getInstance(context)


    // This function sets up the scanning manager
    fun setup() {
        cursorManager.setup()
        MenuManager.getInstance().setup(this, accessibilityService)
    }


    // This function explicitly sets the state of the scanning manager to cursor
    fun setCursorState() {
        ScanReceiver.setState(ScanReceiver.ReceiverState.CURSOR)
        ScanReceiver.isInMenu = false
    }

    // This function explicitly sets the state of the scanning manager to item scan
    fun setItemScanState() {
        ScanReceiver.setState(ScanReceiver.ReceiverState.ITEM_SCAN)
        ScanReceiver.isInMenu = false

        // Start the NodeScanner timeout
        nodeScanner.startTimeoutToRevertToCursor()
    }

    // This function explicitly sets the state of the scanning manager to menu
    fun setMenuState() {
        ScanReceiver.isInMenu = true
    }


    // This function makes a selection
    fun select() {
        if (ScanReceiver.isInMenu) {
            // Select the menu item
            MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.performSelection()
            return
        }

        when (ScanReceiver.getState()) {
            ScanReceiver.ReceiverState.CURSOR -> {
                // Perform the cursor action
                cursorManager.performSelectionAction()
            }

            ScanReceiver.ReceiverState.ITEM_SCAN -> {
                // Perform the item scan action
                nodeScanner.scanTree.performSelection()
            }
        }
    }


    // This function performs an action
    fun performAction(action: SwitchAction) {
        // If swipe lock is enabled, swipe and return
        if (GestureManager.getInstance().performSwipeLock()) {
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
                if (ScanReceiver.isInMenu) {
                    // Stop the menu scanning
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stopScanning()
                    return
                }

                when (ScanReceiver.getState()) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // reset the cursor
                        cursorManager.externalReset()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Stop item scanning
                        nodeScanner.scanTree.stopScanning()
                    }
                }
            }

            SwitchAction.Actions.ACTION_CHANGE_SCANNING_DIRECTION -> {
                if (ScanReceiver.isInMenu) {
                    // Change the menu scanning direction
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.swapScanDirection()
                    return
                }

                when (ScanReceiver.getState()) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // Change the cursor direction
                        cursorManager.swapDirection()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Change the item scan direction
                        nodeScanner.scanTree.swapScanDirection()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_NEXT_ITEM -> {
                if (ScanReceiver.isInMenu) {
                    // Move the menu to the next item
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepForward()
                    return
                }

                when (ScanReceiver.getState()) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // Move the cursor to the next item
                        cursorManager.moveToNextItem()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Move to the next item
                        nodeScanner.scanTree.stepForward()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_PREVIOUS_ITEM -> {
                if (ScanReceiver.isInMenu) {
                    // Move the menu to the previous item
                    MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepBackward()
                    return
                }

                when (ScanReceiver.getState()) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // Move the cursor to the previous item
                        cursorManager.moveToPreviousItem()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Move to the previous item
                        nodeScanner.scanTree.stepBackward()
                    }
                }
            }

            SwitchAction.Actions.ACTION_SYS_HOME -> {
                // Go to the home screen
                accessibilityService.performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }
    }

    fun pauseScanning() {
        if (ScanReceiver.isInMenu) {
            // Pause the menu
            MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.pauseScanning()
            return
        }

        when (ScanReceiver.getState()) {
            ScanReceiver.ReceiverState.CURSOR -> {
                // Pause the cursor
                cursorManager.pauseScanning()
            }

            ScanReceiver.ReceiverState.ITEM_SCAN -> {
                // Pause the item scan
                nodeScanner.scanTree.pauseScanning()
            }
        }
    }

    fun resumeScanning() {
        if (ScanReceiver.isInMenu) {
            // Resume the menu
            MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.resumeScanning()
            return
        }

        when (ScanReceiver.getState()) {
            ScanReceiver.ReceiverState.CURSOR -> {
                // Resume the cursor
                cursorManager.resumeScanning()
            }

            ScanReceiver.ReceiverState.ITEM_SCAN -> {
                // Resume the item scan
                nodeScanner.scanTree.resumeScanning()
            }
        }
    }

    fun shutdown() {
        // Stop scanning
        pauseScanning()

        // Clean up resources
        cursorManager.cleanup()
        nodeScanner.cleanup()
    }
}