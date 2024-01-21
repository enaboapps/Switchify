package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.MenuManager

class SwipeGesturesMenu(accessibilityService: SwitchifyAccessibilityService) : BaseMenu(accessibilityService, buildSwipeGesturesMenuItems(accessibilityService)) {

    companion object {
        private fun buildSwipeGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Swipe Up", action = {
                    GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.UP)
                }),
                MenuItem("Swipe Down", action = {
                    GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.DOWN)
                }),
                MenuItem("Swipe Left", action = {
                    GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.LEFT)
                }),
                MenuItem("Swipe Right", action = {
                    GestureManager.getInstance().performSwipe(GestureManager.SwipeDirection.RIGHT)
                }),
                MenuItem("Lock/Unlock", closeOnSelect = false, action = {
                    GestureManager.getInstance().toggleSwipeLock()
                })
            )
        }
    }
}