package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class MainMenu(accessibilityService: SwitchifyAccessibilityService) {
    private var items: List<MenuItem> = listOf(
        MenuItem("Tap", {
            GestureManager.getInstance().performTap()
        }),
        MenuItem("Swipe", {
            // Perform the swipe action
        }),
        MenuItem("System Control", {
            MenuManager.getInstance().openSystemControlMenu()
        }),
    )

    val menuView = MenuView(accessibilityService, items)

    // This function is called when the menu is opened
    fun open() {
        menuView.open()
    }

    // This function is called when the menu is closed
    fun close() {
        menuView.close()
    }
}