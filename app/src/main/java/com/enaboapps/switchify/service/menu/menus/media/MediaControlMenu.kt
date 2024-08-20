package com.enaboapps.switchify.service.menu.menus.media

import android.accessibilityservice.AccessibilityService
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class MediaControlMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildMediaControlMenuItems(accessibilityService)) {

    companion object {
        private fun buildMediaControlMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                // Play/Pause using headset
                MenuItem("Play/Pause") {
                    accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_KEYCODE_HEADSETHOOK)
                }
            )
        }
    }
}