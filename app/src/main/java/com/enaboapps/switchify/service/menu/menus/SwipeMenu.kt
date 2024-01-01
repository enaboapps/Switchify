package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuView

class SwipeMenu(accessibilityService: SwitchifyAccessibilityService) {
    private var items: List<MenuItem> = listOf(
        MenuItem("Swipe Up", {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.UP)
        }),
        MenuItem("Swipe Down", {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.DOWN)
        }),
        MenuItem("Swipe Left", {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.LEFT)
        }),
        MenuItem("Swipe Right", {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.RIGHT)
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