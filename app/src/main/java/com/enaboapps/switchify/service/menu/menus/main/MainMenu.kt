package com.enaboapps.switchify.service.menu.menus.main

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.MenuManager

class MainMenu(accessibilityService: SwitchifyAccessibilityService) : BaseMenu(accessibilityService, buildMainMenuItems(accessibilityService)) {

    companion object {
        private fun buildMainMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Tap", action = {
                    GestureManager.getInstance().performTap()
                }),
                MenuItem("Gestures", action = {
                    MenuManager.getInstance().openGesturesMenu()
                }),
                MenuItem("System Control", action = {
                    MenuManager.getInstance().openSystemControlMenu()
                })
            )
        }
    }
}