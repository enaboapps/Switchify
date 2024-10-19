package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuView
import com.enaboapps.switchify.service.menu.store.structure.MenuStructureHolder

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
     * Build the system navigation items
     * @return The system navigation items
     */
    fun buildSystemNavItems(): List<MenuItem> {
        return MenuStructureHolder(accessibilityService).systemNavItems
    }

    /**
     * Build the navigation menu items
     * @return The navigation menu items
     */
    fun buildNavMenuItems(): List<MenuItem> {
        return MenuStructureHolder(accessibilityService).menuManipulatorItems
    }

    /**
     * Build the menu view
     * @return The menu view
     */
    fun build(): MenuView {
        return MenuView(accessibilityService, this)
    }
}