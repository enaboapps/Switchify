package com.enaboapps.switchify.service.menu

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.menus.gestures.GesturesMenu
import com.enaboapps.switchify.service.menu.menus.main.MainMenu
import com.enaboapps.switchify.service.menu.menus.gestures.SwipeGesturesMenu
import com.enaboapps.switchify.service.menu.menus.system.SystemControlMenu
import com.enaboapps.switchify.service.menu.menus.gestures.TapGesturesMenu
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
        openMenu(mainMenu.build())
    }

    // This function opens the system control menu
    fun openSystemControlMenu() {
        val systemControlMenu = SystemControlMenu(accessibilityService!!)
        openMenu(systemControlMenu.build())
    }

    // This function opens the gestures menu
    fun openGesturesMenu() {
        val gesturesMenu = GesturesMenu(accessibilityService!!)
        openMenu(gesturesMenu.build())
    }

    // This function opens the tap menu
    fun openTapMenu() {
        val tapGesturesMenu = TapGesturesMenu(accessibilityService!!)
        openMenu(tapGesturesMenu.build())
    }

    // This function opens the swipe menu
    fun openSwipeMenu() {
        val swipeGesturesMenu = SwipeGesturesMenu(accessibilityService!!)
        openMenu(swipeGesturesMenu.build())
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