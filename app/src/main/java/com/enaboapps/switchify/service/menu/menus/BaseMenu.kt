package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

open class BaseMenu(
    private val accessibilityService: SwitchifyAccessibilityService,
    private val items: List<MenuItem>
) {
    private fun getMenuItems(): List<MenuItem> {
        var menuItems = items
        if (MenuManager.getInstance().menuHierarchy?.getTopMenu() != null) {
            menuItems = menuItems + MenuItem("Previous Menu", isMenuHierarchyManipulator = true, action = {
                MenuManager.getInstance().menuHierarchy?.popMenu()
            })
        }
        menuItems = menuItems + MenuItem("Close Menu", isMenuHierarchyManipulator = true, action = {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        })
        return menuItems
    }

    fun build(): MenuView {
        return MenuView(accessibilityService, getMenuItems())
    }
}