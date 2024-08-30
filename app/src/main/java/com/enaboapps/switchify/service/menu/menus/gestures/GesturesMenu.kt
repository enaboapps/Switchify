package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class GesturesMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Tap Gestures", isLinkToMenu = true) {
                    MenuManager.getInstance().openTapMenu()
                },
                MenuItem("Swipe Gestures", isLinkToMenu = true) {
                    MenuManager.getInstance().openSwipeMenu()
                },
                MenuItem("Drag") {
                    GestureManager.getInstance().startDragGesture()
                },
                MenuItem("Zoom Gestures", isLinkToMenu = true) {
                    MenuManager.getInstance().openZoomGesturesMenu()
                },
                MenuItem("Toggle Gesture Lock", closeOnSelect = false) {
                    GestureManager.getInstance().toggleGestureLock()
                }
            )
        }
    }
}