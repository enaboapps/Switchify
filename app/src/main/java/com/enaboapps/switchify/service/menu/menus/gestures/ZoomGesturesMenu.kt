package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class ZoomGesturesMenu(accessibilityService: SwitchifyAccessibilityService) : BaseMenu(accessibilityService, buildZoomGesturesMenuItems(accessibilityService)) {
    companion object {
        private fun buildZoomGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Zoom In", action = {
                    GestureManager.getInstance().performZoomAction(GestureManager.ZoomAction.ZOOM_IN)
                }),
                MenuItem("Zoom Out", action = {
                    GestureManager.getInstance().performZoomAction(GestureManager.ZoomAction.ZOOM_OUT)
                })
            )
        }
    }
}