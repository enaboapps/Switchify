package com.enaboapps.switchify.service.nodes

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.selection.AutoSelectionHandler
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * This class represents a node
 */
class Node : ScanNodeInterface {
    private var nodeInfo: AccessibilityNodeInfo? = null
    private var x: Int = 0
    private var y: Int = 0
    private var centerX: Int = 0
    private var centerY: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var highlighted: Boolean = false

    private var window = SwitchifyAccessibilityWindow.instance

    private var boundsLayout: RelativeLayout? = null

    private val handler = Handler(Looper.getMainLooper())


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
    }

    enum class ActionType {
        CUT, COPY, PASTE
    }

    fun isActionable(actionType: ActionType): Boolean {
        return when (actionType) {
            ActionType.CUT -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CUT)
                ?: false

            ActionType.COPY -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_COPY)
                ?: false

            ActionType.PASTE -> nodeInfo?.actionList?.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_PASTE)
                ?: false
        }
    }

    fun performAction(action: Int) {
        nodeInfo?.performAction(action)
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
        showBounds()
        highlighted = true
    }

    override fun unhighlight() {
        hideBounds()
        highlighted = false
    }

    override fun select() {
        unhighlight()

        GesturePoint.x = centerX
        GesturePoint.y = centerY

        AutoSelectionHandler.setSelectAction {
            GestureManager.getInstance().performTap()
        }
        AutoSelectionHandler.performSelectionAction()
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


    /**
     * This function shows the bounds of the node
     * by rendering a red rectangle around the node
     */
    private fun showBounds() {
        handler.post {
            boundsLayout?.let {
                window.removeView(it)
            }
            boundsLayout = RelativeLayout(window.getContext())
            boundsLayout?.let {
                it.background = window.getContext()?.getDrawable(R.drawable.scan_item_border)
                window.addView(it, x, y, width, height)
            }
        }
    }

    /**
     * This function hides the bounds of the node
     */
    private fun hideBounds() {
        handler.post {
            boundsLayout?.let {
                window.removeView(it)
            }
        }
    }
}