package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.SwipeDirection
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class SwipeGesturesMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildSwipeGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildSwipeGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Swipe Up") {
                    GestureManager.getInstance().performSwipe(SwipeDirection.UP)
                },
                MenuItem("Swipe Down") {
                    GestureManager.getInstance().performSwipe(SwipeDirection.DOWN)
                },
                MenuItem("Swipe Left") {
                    GestureManager.getInstance().performSwipe(SwipeDirection.LEFT)
                },
                MenuItem("Swipe Right") {
                    GestureManager.getInstance().performSwipe(SwipeDirection.RIGHT)
                }
            )
        }
    }
}