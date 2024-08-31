package com.enaboapps.switchify.service.menu.menus.scroll

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.store.MenuItemStore

class ScrollMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildScrollMenuItems(accessibilityService)) {
    companion object {
        private fun buildScrollMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return MenuItemStore(accessibilityService).buildScrollMenuObject().getMenuItems()
        }
    }
}