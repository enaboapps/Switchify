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

    // This enum represents the state of the scanning manager (cursor or menu)
    private enum class State {
        CURSOR,
        MENU
    }

    // This variable represents the current state of the scanning manager
    private var state = State.CURSOR


    // This function sets up the scanning manager
    fun setup() {
        cursorManager.setup()
        MenuManager.getInstance().setup(this, accessibilityService)
    }


    // This function explicitly sets the state of the scanning manager to cursor
    fun setCursorState() {
        state = State.CURSOR
    }

    // This function explicitly sets the state of the scanning manager to menu
    fun setMenuState() {
        state = State.MENU
    }


    // This function makes a selection
    fun select() {
        when (state) {
            State.CURSOR -> {
                // Perform the cursor action
                cursorManager.performSelectionAction()
            }

            State.MENU -> {
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
                when (state) {
                    State.CURSOR -> {
                        // reset the cursor
                        cursorManager.externalReset()
                    }

                    State.MENU -> {
                        // Stop scanning
                        MenuManager.getInstance().closeMenuHierarchy()
                    }
                }
            }

            SwitchAction.Actions.ACTION_CHANGE_SCANNING_DIRECTION -> {
                when (state) {
                    State.CURSOR -> {
                        // Change the cursor direction
                        cursorManager.swapDirection()
                    }

                    State.MENU -> {
                        // Change the menu direction
                        MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.swapScanDirection()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_NEXT_ITEM -> {
                when (state) {
                    State.CURSOR -> {
                        // Move the cursor to the next item
                        cursorManager.moveToNextItem()
                    }

                    State.MENU -> {
                        // Move the menu to the next item
                        MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.stepForward()
                    }
                }
            }

            SwitchAction.Actions.ACTION_MOVE_TO_PREVIOUS_ITEM -> {
                when (state) {
                    State.CURSOR -> {
                        // Move the cursor to the previous item
                        cursorManager.moveToPreviousItem()
                    }

                    State.MENU -> {
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
        when (state) {
            State.CURSOR -> {
                // Pause the cursor
                cursorManager.pauseScanning()
            }

            State.MENU -> {
                // Pause the menu
                MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.pauseScanning()
            }
        }
    }

    fun resumeScanning() {
        when (state) {
            State.CURSOR -> {
                // Resume the cursor
                cursorManager.resumeScanning()
            }

            State.MENU -> {
                // Resume the menu
                MenuManager.getInstance().menuHierarchy?.getTopMenu()?.scanTree?.resumeScanning()
            }
        }
    }
}