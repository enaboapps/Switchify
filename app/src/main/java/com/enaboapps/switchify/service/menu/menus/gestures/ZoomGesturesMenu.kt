package com.enaboapps.switchify.service.menu.menus.gestures

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.ZoomGesturePerformer
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu

class ZoomGesturesMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildZoomGesturesMenuItems(accessibilityService)) {
    companion object {
        private fun buildZoomGesturesMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            return listOf(
                MenuItem("Zoom In") {
                    GestureManager.getInstance()
                        .performZoomAction(ZoomGesturePerformer.ZoomAction.ZOOM_IN)
                },
                MenuItem("Zoom Out") {
                    GestureManager.getInstance()
                        .performZoomAction(ZoomGesturePerformer.ZoomAction.ZOOM_OUT)
                }
            )
        }
    }
}