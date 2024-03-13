package com.enaboapps.switchify.service.menu.menus.system

import android.accessibilityservice.AccessibilityService
import android.content.pm.PackageManager
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class DeviceMenu(
    accessibilityService: SwitchifyAccessibilityService
) : BaseMenu(accessibilityService, buildDeviceItems(accessibilityService)) {

    companion object {
        private fun buildDeviceItems(accessibilityService: AccessibilityService): List<MenuItem> {
            val packageManager = accessibilityService.packageManager
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
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS)) {
                    MenuItem("Toggle Split Screen") {
                        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
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