package com.enaboapps.switchify.service.menu

import android.os.Handler
import android.os.Looper
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.menus.MainMenu
import com.enaboapps.switchify.service.menu.menus.SwipeMenu
import com.enaboapps.switchify.service.menu.menus.SystemControlMenu
import com.enaboapps.switchify.service.scanning.ScanningManager
import java.util.concurrent.DelayQueue

class MenuManager : MenuViewListener {

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
    var scanningManager: ScanningManager? = null

    // accessibility service
    var accessibilityService: SwitchifyAccessibilityService? = null

    // Variable to keep track of the current menu
    var currentMenu: MenuView? = null

    // This function opens the main menu
    fun openMainMenu() {
        val mainMenu = MainMenu(accessibilityService!!)
        openMenu(mainMenu.menuView)
    }

    // This function opens the system control menu
    fun openSystemControlMenu() {
        val systemControlMenu = SystemControlMenu(accessibilityService!!)
        openDifferentMenu(systemControlMenu.menuView)
    }

    // This function opens the swipe menu
    fun openSwipeMenu() {
        val swipeMenu = SwipeMenu(accessibilityService!!)
        openDifferentMenu(swipeMenu.menuView)
    }

    // This function opens a menu
    private fun openMenu(menu: MenuView) {
        if (currentMenu != null) {
            // Close the current menu
            currentMenu?.close()
        }
        // Set the current menu
        currentMenu = menu
        // Set the menu listener
        currentMenu?.menuViewListener = this
        // Open the menu
        currentMenu?.open()
        // Set the scanning manager state to menu
        scanningManager?.setMenuState()
    }

    // This function opens a different menu (close the current menu and open the new menu)
    private fun openDifferentMenu(menu: MenuView) {
        // Close the current menu
        currentMenu?.close()
        // Set the current menu to null
        currentMenu = null
        Handler(Looper.getMainLooper()).postDelayed({
            // Open the new menu
            openMenu(menu)
        }, 100)
    }

    // This function is called when the menu is closed
    override fun onMenuViewClosed() {
        // Set the current menu to null
        currentMenu = null
        // Set the scanning manager state to cursor
        scanningManager?.setCursorState()
    }


}