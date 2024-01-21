package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.MenuManager

class TapGesturesMenu(accessibilityService: SwitchifyAccessibilityService) : BaseMenu(accessibilityService, buildTapGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildTapGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Tap", action = {
                    GestureManager.getInstance().performTap()
                }),
                MenuItem("Double Tap", action = {
                    GestureManager.getInstance().performDoubleTap()
                })
            )
        }
    }
}