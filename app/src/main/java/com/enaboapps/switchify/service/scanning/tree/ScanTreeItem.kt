package com.enaboapps.switchify.service.scanning.tree

import com.enaboapps.switchify.service.nodes.NodeScannerUI
import com.enaboapps.switchify.service.scanning.ScanNodeInterface

/**
 * This class represents an item in the scan tree
 * @property children The children of the item
 * @property y The y coordinate of the item
 * @property individualHighlighting If the children should be highlighted individually
 */
class ScanTreeItem(
    val children: List<ScanNodeInterface>,
    val y: Int,
    private val individualHighlighting: Boolean
) {
    /**
     * This function highlights the item
     */
    fun highlight() {
        if (individualHighlighting || children.size == 1) {
            children.forEach { it.highlight() }
        } else {
            unhighlight()
            NodeScannerUI.instance.showRowBounds(getX(), y, getWidth(), getHeight())
        }
    }

    /**
     * This function unhighlights the item
     */
    fun unhighlight() {
        if (individualHighlighting || children.size == 1) {
            children.forEach { it.unhighlight() }
        } else {
            NodeScannerUI.instance.hideRowBounds()
        }
    }

    /**
     * This function gets the x coordinate of the item
     * @return The x coordinate of the item
     */
    private fun getX(): Int {
        var minX = Int.MAX_VALUE
        children.forEach {
            if (it.getLeft() < minX) {
                minX = it.getLeft()
            }
        }
        return minX
    }

    /**
     * This function gets the width of the item
     * @return The width of the item
     */
    private fun getWidth(): Int {
        val firstX = children.minOfOrNull { it.getLeft() } ?: 0
        val lastX = children.maxOfOrNull { it.getLeft() + it.getWidth() } ?: 0
        return lastX - firstX
    }

    /**
     * This function gets the height of the item
     * @return The height of the item
     */
    private fun getHeight(): Int {
        val minY = children.minOfOrNull { it.getTop() } ?: 0
        val maxY = children.maxOfOrNull { it.getTop() + it.getHeight() } ?: 0
        return maxY - minY
    }
}