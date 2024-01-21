package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class SwipeMenu(accessibilityService: SwitchifyAccessibilityService) {
    private val items: List<MenuItem> = listOf(
        MenuItem("Swipe Up", action = {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.UP)
        }),
        MenuItem("Swipe Down", action = {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.DOWN)
        }),
        MenuItem("Swipe Left", action = {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.LEFT)
        }),
        MenuItem("Swipe Right", action = {
            GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.RIGHT)
        }),
        MenuItem("Lock/Unlock", closeOnSelect = false, action = {
            GestureManager.getInstance().toggleSwipeLock()
        }),
        MenuItem("Previous Menu", action = {
            MenuManager.getInstance().menuHierarchy?.popMenu()
        }),
        MenuItem("Close Menu", action = {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        })
    )

    val menuView = MenuView(accessibilityService, items)
    
}