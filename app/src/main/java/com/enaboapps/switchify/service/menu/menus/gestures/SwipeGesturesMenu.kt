package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.store.MenuItemStore

class SwipeGesturesMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildSwipeGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildSwipeGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return MenuItemStore(accessibilityService).swipeGesturesMenuObject.getMenuItems()
        }
    }
}