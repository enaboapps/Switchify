package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class TapGesturesMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildTapGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildTapGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Tap") {
                    GestureManager.getInstance().performTap()
                },
                MenuItem("Double Tap") {
                    GestureManager.getInstance().performDoubleTap()
                },
                MenuItem("Tap and Hold") {
                    GestureManager.getInstance().performTapAndHold()
                }
            )
        }
    }
}