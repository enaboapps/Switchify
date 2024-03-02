package com.enaboapps.switchify.service.nodes

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * This class represents a node
 */
class Node : ScanNodeInterface {
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
            node.x = rect.left
            node.y = rect.top
            node.centerX = rect.centerX()
            node.centerY = rect.centerY()
            node.width = rect.width()
            node.height = rect.height()
            return node
        }
    }

    override fun getX(): Int {
        return x
    }

    override fun getY(): Int {
        return y
    }

    fun getCenterX(): Int {
        return centerX
    }

    fun getCenterY(): Int {
        return centerY
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
        TODO("Not yet implemented")
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
                it.setBackgroundColor(0x55FF0000)
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
            boundsLayout = null
        }
    }
}