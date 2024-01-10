package com.enaboapps.switchify.service.menu

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.menus.GesturesMenu
import com.enaboapps.switchify.service.menu.menus.MainMenu
import com.enaboapps.switchify.service.menu.menus.SwipeMenu
import com.enaboapps.switchify.service.menu.menus.SystemControlMenu
import com.enaboapps.switchify.service.scanning.ScanningManager

class MenuManager {
    // singleton
    companion object {
        private var instance: MenuManager? = null
        fun getInstance(): MenuManager {
            if (instance == null) {
                instance = MenuManager()
            }
            return instance!!
        }
    }

    // scanning manager
    private var scanningManager: ScanningManager? = null

    // accessibility service
    private var accessibilityService: SwitchifyAccessibilityService? = null

    // Hierarchy
    var menuHierarchy: MenuHierarchy? = null

    // This function sets up the menu manager
    fun setup(scanningManager: ScanningManager, accessibilityService: SwitchifyAccessibilityService) {
        this.scanningManager = scanningManager
        menuHierarchy = MenuHierarchy(scanningManager)
        this.accessibilityService = accessibilityService
    }

    // This function opens the main menu
    fun openMainMenu() {
        val mainMenu = MainMenu(accessibilityService!!)
        openMenu(mainMenu.menuView)
    }

    // This function opens the system control menu
    fun openSystemControlMenu() {
        val systemControlMenu = SystemControlMenu(accessibilityService!!)
        openMenu(systemControlMenu.menuView)
    }

    // This function opens the gestures menu
    fun openGesturesMenu() {
        val gesturesMenu = GesturesMenu(accessibilityService!!)
        openMenu(gesturesMenu.menuView)
    }

    // This function opens the swipe menu
    fun openSwipeMenu() {
        val swipeMenu = SwipeMenu(accessibilityService!!)
        openMenu(swipeMenu.menuView)
    }

    // This function opens a menu
    private fun openMenu(menu: MenuView) {
        // Add the menu to the hierarchy
        menuHierarchy?.openMenu(menu)
    }

    // This function closes the menu hierarchy
    fun closeMenuHierarchy() {
        menuHierarchy?.removeAllMenus()
    }
}