package com.enaboapps.switchify.service.menu.menus

import android.accessibilityservice.AccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class SystemControlMenu(accessibilityService: AccessibilityService) {

    private var items: List<MenuItem> = listOf(
        MenuItem("Back", {
            // Perform the back action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }),
        MenuItem("Home", {
            // Perform the home action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        }),
        MenuItem("Recents", {
            // Perform the recents action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }),
        MenuItem("Notifications", {
            // Perform the notifications action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
        }),
        MenuItem("Quick Settings", {
            // Perform the quick settings action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
        }),
        MenuItem("Previous Menu", {
            MenuManager.getInstance().menuHierarchy?.popMenu()
        }),
        MenuItem("Close Menu", {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        })
    )

    val menuView = MenuView(accessibilityService, items.toMutableList())

    // This function is called when the menu is opened
    fun open() {
        menuView.open()
    }

    // This function is called when the menu is closed
    fun close() {
        menuView.close()
    }

}