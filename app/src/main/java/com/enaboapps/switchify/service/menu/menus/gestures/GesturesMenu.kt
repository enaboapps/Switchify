package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.MenuManager

class GesturesMenu(accessibilityService: SwitchifyAccessibilityService) : BaseMenu(accessibilityService, buildGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Tap", action = {
                    MenuManager.getInstance().openTapMenu()
                }),
                MenuItem("Swipe", action = {
                    MenuManager.getInstance().openSwipeMenu()
                })
            )
        }
    }
}