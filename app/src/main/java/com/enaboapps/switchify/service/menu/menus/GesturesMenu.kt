package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class GesturesMenu(accessibilityService: SwitchifyAccessibilityService) {
    private var items: List<MenuItem> = listOf(
        MenuItem("Tap", {
            GestureManager.getInstance().performTap()
        }),
        MenuItem("Double Tap", {
            GestureManager.getInstance().performDoubleTap()
        }),
        MenuItem("Swipe", {
            MenuManager.getInstance().openSwipeMenu()
        }),
        MenuItem("Previous Menu", {
            MenuManager.getInstance().menuHierarchy?.popMenu()
        }),
        MenuItem("Close Menu", {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        })
    )

    val menuView = MenuView(accessibilityService, items.toMutableList())

    // This function is called when the menu is opened
    fun open() {
        menuView.open()
    }

    // This function is called when the menu is closed
    fun close() {
        menuView.close()
    }
}