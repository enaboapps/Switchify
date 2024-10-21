package com.enaboapps.switchify.service.menu.menus.system

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.structure.MenuStructureHolder

class VolumeControlMenu(private val accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildVolumeControlMenuItems(accessibilityService)) {

    companion object {
        private fun buildVolumeControlMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return MenuStructureHolder(accessibilityService).buildVolumeControlMenuObject()
                .getMenuItems()
        }
    }
}