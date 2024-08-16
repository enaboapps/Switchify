package com.enaboapps.switchify.service.menu.menus.scroll

import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.nodes.Node
import com.enaboapps.switchify.service.nodes.NodeExaminer

class ScrollMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildScrollMenuItems(accessibilityService)) {
    companion object {
        private fun buildScrollMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            val currentPoint = GesturePoint.getPoint()
            val scrollUpNode =
                NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_UP)
            val scrollDownNode =
                NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_DOWN)
            val scrollLeftNode =
                NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_LEFT)
            val scrollRightNode =
                NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_RIGHT)
            return listOfNotNull(
                if (scrollUpNode != null) {
                    MenuItem("Scroll Up", closeOnSelect = false) {
                        scrollUpNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id)
                    }
                } else null,
                if (scrollDownNode != null) {
                    MenuItem("Scroll Down", closeOnSelect = false) {
                        scrollDownNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id)
                    }
                } else null,
                if (scrollLeftNode != null) {
                    MenuItem("Scroll Left", closeOnSelect = false) {
                        scrollLeftNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id)
                    }
                } else null,
                if (scrollRightNode != null) {
                    MenuItem("Scroll Right", closeOnSelect = false) {
                        scrollRightNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id)
                    }
                } else null
            )
        }
    }
}