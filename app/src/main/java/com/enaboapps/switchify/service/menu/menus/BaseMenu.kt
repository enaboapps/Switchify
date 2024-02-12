package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.R
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
            menuItems = menuItems + MenuItem(
                drawableId = R.drawable.ic_previous_menu,
                isMenuHierarchyManipulator = true
            ) {
                MenuManager.getInstance().menuHierarchy?.popMenu()
            }
        }
        menuItems = menuItems + MenuItem(
            drawableId = R.drawable.ic_close_menu,
            isMenuHierarchyManipulator = true
        ) {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        }
        return menuItems
    }

    fun build(): MenuView {
        return MenuView(accessibilityService, getMenuItems())
    }
}