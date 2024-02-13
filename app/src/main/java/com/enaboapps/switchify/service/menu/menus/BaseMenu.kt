package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

/**
 * This class represents a base menu
 * @property accessibilityService The accessibility service
 * @property items The menu items
 */
open class BaseMenu(
    private val accessibilityService: SwitchifyAccessibilityService,
    private val items: List<MenuItem>
) {
    /**
     * Get the menu items
     * @return The menu items
     */
    fun getMenuItems(): List<MenuItem> {
        return items
    }

    /**
     * Build the navigation menu items
     * @return The navigation menu items
     */
    fun buildNavMenuItems(): List<MenuItem> {
        val navMenuItems = mutableListOf<MenuItem>()
        if (MenuManager.getInstance().menuHierarchy?.getTopMenu() != null) {
            navMenuItems.add(
                MenuItem(
                    drawableId = R.drawable.ic_previous_menu,
                    isMenuHierarchyManipulator = true,
                    action = { MenuManager.getInstance().menuHierarchy?.popMenu() }
                )
            )
        }
        navMenuItems.add(
            MenuItem(
                drawableId = R.drawable.ic_close_menu,
                isMenuHierarchyManipulator = true,
                action = { MenuManager.getInstance().menuHierarchy?.removeAllMenus() }
            )
        )
        return navMenuItems
    }

    /**
     * Build the menu view
     * @return The menu view
     */
    fun build(): MenuView {
        return MenuView(accessibilityService, this)
    }
}