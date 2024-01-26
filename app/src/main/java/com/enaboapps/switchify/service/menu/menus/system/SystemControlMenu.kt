package com.enaboapps.switchify.service.menu.menus.system

import android.accessibilityservice.AccessibilityService
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class SystemControlMenu(
    accessibilityService: SwitchifyAccessibilityService
) : BaseMenu(accessibilityService, buildSystemControlItems(accessibilityService)) {

    companion object {
        private fun buildSystemControlItems(accessibilityService: AccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Back", action = {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                }),
                MenuItem("Home", action = {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                }),
                MenuItem("Recents", action = {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                }),
                MenuItem("Notifications", action = {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                }),
                MenuItem("Quick Settings", action = {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                }),
                MenuItem("Power Dialog", action = {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
                }),
                MenuItem("Volume Control", isMenuNavItem = true, action = {
                    MenuManager.getInstance().openVolumeControlMenu()
                })
            )
        }
    }
}