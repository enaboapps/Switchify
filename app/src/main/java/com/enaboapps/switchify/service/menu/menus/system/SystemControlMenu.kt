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
            return listOfNotNull(
                MenuItem("Back") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                },
                MenuItem("Home") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                },
                MenuItem("Recents") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
                },
                MenuItem("Notifications") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                },
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MenuItem("All Apps") {
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS)
                    }
                } else null,
                MenuItem("Quick Settings") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                },
                MenuItem("Lock Screen") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                },
                MenuItem("Power Dialog") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG)
                },
                MenuItem("Volume Control", isLinkToMenu = true) {
                    MenuManager.getInstance().openVolumeControlMenu()
                }
            )
        }
    }
}