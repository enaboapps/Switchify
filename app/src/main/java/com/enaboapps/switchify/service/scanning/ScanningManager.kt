package com.enaboapps.switchify.service.scanning

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.content.Context
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.switches.SwitchAction

class ScanningManager(
    private val accessibilityService: SwitchifyAccessibilityService,
    val context: Context
) {

    // cursor manager
    private val cursorManager = CursorManager(context)


    // This function sets up the scanning manager
    fun setup() {
        cursorManager.setup()
        MenuManager.getInstance().setup(this, accessibilityService)
    }


    // This function explicitly sets the state of the scanning manager to cursor
    fun setCursorState() {
        ScanReceiver.state = ScanReceiver.ReceiverState.CURSOR
    }

    // This function explicitly sets the state of the scanning manager to item scan
    fun setItemScanState() {
        ScanReceiver.state = ScanReceiver.ReceiverState.ITEM_SCAN
    }

    // This function explicitly sets the state of the scanning manager to menu
    fun setMenuState() {
        ScanReceiver.state = ScanReceiver.ReceiverState.MENU
    }


    // This function makes a selection
    fun select() {
        when (ScanReceiver.state) {
            ScanReceiver.ReceiverState.CURSOR -> {
                // Perform the cursor action
                cursorManager.performSelectionAction()
            }

            ScanReceiver.ReceiverState.ITEM_SCAN -> {
                // Perform the item scan action
            }

            ScanReceiver.ReceiverState.MENU -> {
                // Select the menu item
                MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.performSelection()
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
                when (ScanReceiver.state) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // reset the cursor
                        cursorManager.externalReset()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Stop item scanning
                    }

                    ScanReceiver.ReceiverState.MENU -> {
                        // Stop scanning
                        MenuManager.getInstance().closeMenuHierarchy()
                    }
                }
            }

            SwitchAction.Actions.ACTION_CHANGE_SCANNING_DIRECTION -> {
                when (ScanReceiver.state) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // Change the cursor direction
                        cursorManager.swapDirection()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Change the item scan direction
                    }

                    ScanReceiver.ReceiverState.MENU -> {
                        // Change the menu direction
                        MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.swapScanDirection()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_NEXT_ITEM -> {
                when (ScanReceiver.state) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // Move the cursor to the next item
                        cursorManager.moveToNextItem()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Move to the next item
                    }

                    ScanReceiver.ReceiverState.MENU -> {
                        // Move the menu to the next item
                        MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepForward()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_PREVIOUS_ITEM -> {
                when (ScanReceiver.state) {
                    ScanReceiver.ReceiverState.CURSOR -> {
                        // Move the cursor to the previous item
                        cursorManager.moveToPreviousItem()
                    }

                    ScanReceiver.ReceiverState.ITEM_SCAN -> {
                        // Move to the previous item
                    }

                    ScanReceiver.ReceiverState.MENU -> {
                        // Move the menu to the previous item
                        MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepBackward()
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
        when (ScanReceiver.state) {
            ScanReceiver.ReceiverState.CURSOR -> {
                // Pause the cursor
                cursorManager.pauseScanning()
            }

            ScanReceiver.ReceiverState.ITEM_SCAN -> {
                // Pause the item scan
            }

            ScanReceiver.ReceiverState.MENU -> {
                // Pause the menu
                MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.pauseScanning()
            }
        }
    }

    fun resumeScanning() {
        when (ScanReceiver.state) {
            ScanReceiver.ReceiverState.CURSOR -> {
                // Resume the cursor
                cursorManager.resumeScanning()
            }

            ScanReceiver.ReceiverState.ITEM_SCAN -> {
                // Resume the item scan
            }

            ScanReceiver.ReceiverState.MENU -> {
                // Resume the menu
                MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.resumeScanning()
            }
        }
    }
}