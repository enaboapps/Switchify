package com.enaboapps.switchify.service.scanning

import android.content.Context
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView
import com.enaboapps.switchify.service.menu.MenuViewListener
import com.enaboapps.switchify.service.menu.menus.MainMenu
import com.enaboapps.switchify.service.menu.menus.SystemControlMenu
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
        MenuManager.getInstance().scanningManager = this
        MenuManager.getInstance().accessibilityService = accessibilityService
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
                cursorManager.performAction()
            }

            State.MENU -> {
                // Select the menu item
                MenuManager.getInstance().currentMenu?.select()
            }
        }
    }


    // This function performs an action
    fun performAction(action: SwitchAction) {
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
                        MenuManager.getInstance().currentMenu?.close()
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
                        MenuManager.getInstance().currentMenu?.swapScanDirection()
                    }
                }
            }
        }
    }
}