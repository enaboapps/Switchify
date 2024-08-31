package com.enaboapps.switchify.service.menu.menus.media

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.store.MenuItemStore

class MediaControlMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildMediaControlMenuItems(accessibilityService)) {

    companion object {
        private fun buildMediaControlMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return MenuItemStore(accessibilityService).mediaControlMenuObject.getMenuItems()
        }
    }
}