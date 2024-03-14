package com.enaboapps.switchify.service.menu.menus.main

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.nodes.NodeExaminer
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.utils.KeyboardInfo

class MainMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildMainMenuItems(accessibilityService)) {

    companion object {
        private fun buildMainMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            val menuItems = mutableListOf<MenuItem>()

            menuItems.add(MenuItem("Tap") {
                GestureManager.getInstance().performTap()
            })

            menuItems.add(MenuItem("Gestures", isLinkToMenu = true) {
                MenuManager.getInstance().openGesturesMenu()
            })

            // Only add "Refine Selection" if the current scan method is not item scan
            if (ScanMethod.getType() != ScanMethod.MethodType.ITEM_SCAN) {
                menuItems.add(MenuItem("Refine Selection") {
                    GesturePoint.setReselect(true)
                })
            }

            menuItems.add(MenuItem("Device", isLinkToMenu = true) {
                MenuManager.getInstance().openDeviceMenu()
            })

            val canEdit = NodeExaminer.canPerformEditActions(GesturePoint.getPoint())
            if (canEdit) {
                menuItems.add(MenuItem("Edit", isLinkToMenu = true) {
                    MenuManager.getInstance().openEditMenu()
                })
            }

            if (!KeyboardInfo.isKeyboardVisible) {
                menuItems.add(MenuItem(MenuManager.getInstance().getTypeToSwitchTo()) {
                    MenuManager.getInstance().changeBetweenCursorAndItemScan()
                })
            }

            return menuItems
        }
    }
}