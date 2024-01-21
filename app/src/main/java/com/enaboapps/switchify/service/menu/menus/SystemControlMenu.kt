package com.enaboapps.switchify.service.menu.menus

import android.accessibilityservice.AccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.MenuView

class SystemControlMenu(accessibilityService: AccessibilityService) {

    private val items: List<MenuItem> = listOf(
        MenuItem("Back", action = {
            // Perform the back action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }),
        MenuItem("Home", action = {
            // Perform the home action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        }),
        MenuItem("Recents", action = {
            // Perform the recents action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }),
        MenuItem("Notifications", action = {
            // Perform the notifications action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
        }),
        MenuItem("Quick Settings", action = {
            // Perform the quick settings action
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
        }),
        MenuItem("Previous Menu", action = {
            MenuManager.getInstance().menuHierarchy?.popMenu()
        }),
        MenuItem("Close Menu", action = {
            MenuManager.getInstance().menuHierarchy?.removeAllMenus()
        })
    )

    val menuView = MenuView(accessibilityService, items)

}