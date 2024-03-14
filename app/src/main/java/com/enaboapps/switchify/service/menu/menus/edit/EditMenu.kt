package com.enaboapps.switchify.service.menu.menus.edit

import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.nodes.Node
import com.enaboapps.switchify.service.nodes.NodeExaminer

class EditMenu(accessibilityService: SwitchifyAccessibilityService) :
    BaseMenu(accessibilityService, buildEditMenuItems(accessibilityService)) {
    companion object {
        private fun buildEditMenuItems(accessibilityService: SwitchifyAccessibilityService): List<MenuItem> {
            val currentPoint = GesturePoint.getPoint()
            val cutNode = NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.CUT)
            val copyNode = NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.COPY)
            val pasteNode = NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.PASTE)
            return listOfNotNull(
                if (cutNode != null) {
                    MenuItem("Cut") {
                        cutNode.performAction(AccessibilityNodeInfo.ACTION_CUT)
                    }
                } else null,
                if (copyNode != null) {
                    MenuItem("Copy") {
                        copyNode.performAction(AccessibilityNodeInfo.ACTION_COPY)
                    }
                } else null,
                if (pasteNode != null) {
                    MenuItem("Paste") {
                        pasteNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                    }
                } else null
            )
        }
    }
}