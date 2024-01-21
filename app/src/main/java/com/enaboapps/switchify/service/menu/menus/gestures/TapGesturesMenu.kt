package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class TapGesturesMenu(private val accessibilityService: SwitchifyAccessibilityService) {
    private val items: List<MenuItem> = listOf(
        MenuItem("Tap", action = {
            GestureManager.getInstance().performTap()
        }),
        MenuItem("Double Tap", action = {
            GestureManager.getInstance().performDoubleTap()
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