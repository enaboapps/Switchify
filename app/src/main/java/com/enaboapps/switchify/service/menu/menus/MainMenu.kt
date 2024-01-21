package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class MainMenu(accessibilityService: SwitchifyAccessibilityService) {
    private val items: List<MenuItem> = listOf(
        MenuItem("Tap", action = {
            GestureManager.getInstance().performTap()
        }),
        MenuItem("Gestures", action = {
            MenuManager.getInstance().openGesturesMenu()
        }),
        MenuItem("System Control", action = {
            MenuManager.getInstance().openSystemControlMenu()
        }),
        MenuItem("Close Menu", action = {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        })
    )

    val menuView = MenuView(accessibilityService, items)

}