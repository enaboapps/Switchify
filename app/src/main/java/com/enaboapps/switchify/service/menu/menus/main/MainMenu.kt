package com.enaboapps.switchify.service.menu.menus.main

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.cursor.CursorPoint
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class MainMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildMainMenuItems(accessibilityService)) {

    companion object {
        private fun buildMainMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Tap") {
                    GestureManager.getInstance().performTap()
                },
                MenuItem("Gestures", isLinkToMenu = true) {
                    MenuManager.getInstance().openGesturesMenu()
                },
                MenuItem("Refine Selection") {
                    CursorPoint.setReselect(true)
                },
                MenuItem("System Control", isLinkToMenu = true) {
                    MenuManager.getInstance().openSystemControlMenu()
                },
                MenuItem(MenuManager.getInstance().getStateToSwitchTo()) {
                    MenuManager.getInstance().changeBetweenCursorAndItemScan()
                }
            )
        }
    }
}