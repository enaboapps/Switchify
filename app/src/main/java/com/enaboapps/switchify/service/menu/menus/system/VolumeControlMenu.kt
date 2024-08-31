package com.enaboapps.switchify.service.menu.menus.system

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.store.MenuItemStore

class VolumeControlMenu(private val accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildVolumeControlMenuItems(accessibilityService)) {

    companion object {
        private fun buildVolumeControlMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return MenuItemStore(accessibilityService).buildVolumeControlMenuObject().getMenuItems()
        }
    }
}