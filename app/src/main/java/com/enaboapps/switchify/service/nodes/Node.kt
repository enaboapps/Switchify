package com.enaboapps.switchify.service.nodes

import android.graphics.PointF
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.keyboard.KeyInfo
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.selection.AutoSelectionHandler

/**
 * This class represents a node
 */
class Node(
    private var select: (() -> Unit?)? = null
) : ScanNodeInterface {
    private var nodeInfo: AccessibilityNodeInfo? = null
    private var x: Int = 0
    private var y: Int = 0
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var highlighted: Boolean = false


    companion object {
        /**
         * This function creates a node from AccessibilityNodeInfo
         * @param nodeInfo The AccessibilityNodeInfo
         * @return The node
         */
        fun fromAccessibilityNodeInfo(nodeInfo: AccessibilityNodeInfo): Node {
            val node = Node()
            val rect = Rect()
            nodeInfo.getBoundsInScreen(rect)
            node.nodeInfo = nodeInfo
            node.x = rect.left
            node.y = rect.top
            node.centerX = rect.centerX()
            node.centerY = rect.centerY()
            node.width = rect.width()
            node.height = rect.height()
            return node
        }

        /**
         * This function creates a node from a KeyInfo object
         * @param keyInfo The KeyInfo object
         * @return The node
         */
        fun fromKeyInfo(keyInfo: KeyInfo): Node {
            val node = Node()
            node.x = keyInfo.x
            node.y = keyInfo.y
            node.centerX = keyInfo.x + keyInfo.width / 2
            node.centerY = keyInfo.y + keyInfo.height / 2
            node.width = keyInfo.width
            node.height = keyInfo.height
            return node
        }

        /**
         * This function creates a node from a MenuItem object
         * @param menuItem The MenuItem object
         * @return The node
         */
        fun fromMenuItem(menuItem: MenuItem): Node {
            val node = Node { menuItem.select() }
            node.x = menuItem.x
            node.y = menuItem.y
            node.centerX = menuItem.x + menuItem.width / 2
            node.centerY = menuItem.y + menuItem.height / 2
            node.width = menuItem.width
            node.height = menuItem.height
            return node
        }
    }

    enum class ActionType {
        CUT,
        COPY,
        PASTE,
        SCROLL_UP,
        SCROLL_DOWN,
        SCROLL_LEFT,
        SCROLL_RIGHT
    }

    fun isActionable(actionType: ActionType): Boolean {
        return when (actionType) {
            ActionType.CUT -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CUT)
                ?: false

            ActionType.COPY -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_COPY)
                ?: false

            ActionType.PASTE -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_PASTE)
                ?: false

            ActionType.SCROLL_UP -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP)
                ?: false

            ActionType.SCROLL_DOWN -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN)
                ?: false

            ActionType.SCROLL_LEFT -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT)
                ?: false

            ActionType.SCROLL_RIGHT -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT)
                ?: false
        }
    }

    /**
     * This function performs an action on the node
     *
     * @param action The action to perform
     */
    fun performAction(action: Int) {
        nodeInfo?.performAction(action)
    }

    /**
     * This function returns whether the node contains a point
     *
     * @param point The point to check
     * @return True if the node contains the point, false otherwise
     */
    fun containsPoint(point: PointF): Boolean {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height
    }

    override fun getMidX(): Int {
        return centerX
    }

    override fun getMidY(): Int {
        return centerY
    }

    override fun getLeft(): Int {
        return x
    }

    override fun getTop(): Int {
        return y
    }

    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }

    override fun highlight() {
        NodeScannerUI.instance.showItemBounds(x, y, width, height)
        highlighted = true
    }

    override fun unhighlight() {
        NodeScannerUI.instance.hideItemBounds()
        highlighted = false
    }

    override fun select() {
        unhighlight()

        if (select == null) {
            GesturePoint.x = centerX
            GesturePoint.y = centerY

            AutoSelectionHandler.setSelectAction {
                GestureManager.getInstance().performTap()
            }
            AutoSelectionHandler.performSelectionAction()
        } else {
            select?.invoke()
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (centerX != other.centerX) return false
        if (centerY != other.centerY) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + centerX
        result = 31 * result + centerY
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}