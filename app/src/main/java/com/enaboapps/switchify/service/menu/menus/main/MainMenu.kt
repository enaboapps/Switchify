package com.enaboapps.switchify.service.menu.menus.main

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.store.MenuItemStore

class MainMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildMainMenuItems(accessibilityService)) {

    companion object {
        private fun buildMainMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return MenuItemStore(accessibilityService).mainMenuObject.getMenuItems()
        }
    }
}