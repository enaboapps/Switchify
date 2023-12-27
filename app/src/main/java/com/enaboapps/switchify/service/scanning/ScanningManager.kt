package com.enaboapps.switchify.service.scanning

import android.content.Context
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView
import com.enaboapps.switchify.service.menu.MenuViewListener
import com.enaboapps.switchify.service.menu.menus.MainMenu
import com.enaboapps.switchify.service.menu.menus.SystemControlMenu

class ScanningManager(
    val accessibilityService: SwitchifyAccessibilityService,
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
}