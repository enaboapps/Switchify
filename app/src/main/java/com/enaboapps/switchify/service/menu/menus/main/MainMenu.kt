package com.enaboapps.switchify.service.menu.menus.main

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorPoint
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
                MenuItem("Gestures", isLinkToMenu = true, action = {
                    MenuManager.getInstance().openGesturesMenu()
                }),
                MenuItem("Refine Selection", action = {
                    CursorPoint.instance.setReselect(true)
                }),
                MenuItem("System Control", isLinkToMenu = true, action = {
                    MenuManager.getInstance().openSystemControlMenu()
                })
            )
        }
    }
}