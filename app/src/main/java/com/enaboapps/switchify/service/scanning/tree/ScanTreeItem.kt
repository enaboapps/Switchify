package com.enaboapps.switchify.service.scanning.tree

import com.enaboapps.switchify.service.methods.nodes.NodeScannerUI
import com.enaboapps.switchify.service.scanning.ScanNodeInterface

/**
 * This class represents an item in the 2D scan tree
 * @property children The children of the item
 * @property y The y coordinate of the item
 * @property isGroupScanEnabled Whether group scanning is enabled (used for splitting logic)
 */
class ScanTreeItem(
    val children: List<ScanNodeInterface>,
    val y: Int,
    private val isGroupScanEnabled: Boolean
) {
    private val groups: List<List<ScanNodeInterface>> = splitIntoGroups(children)

    /**
     * This function highlights the item, group, or specific node
     * @param groupIndex The index of the group to highlight, or null to highlight the entire item
     * @param nodeIndex The index of the node within the group to highlight, or null to highlight the entire group
     */
    fun highlight(groupIndex: Int? = null, nodeIndex: Int? = null) {
        when {
            groupIndex == null && nodeIndex == null -> highlightEntireItem()
            groupIndex != null && nodeIndex == null -> highlightGroup(groupIndex)
            groupIndex != null && nodeIndex != null -> highlightNode(groupIndex, nodeIndex)
            else -> throw IllegalArgumentException("Invalid highlight parameters")
        }
    }

    private fun highlightEntireItem() {
        NodeScannerUI.instance.showRowBounds(getX(), y, getWidth(), getHeight())
    }

    private fun highlightGroup(groupIndex: Int) {
        val group = groups.getOrNull(groupIndex) ?: return
        val groupX = group.minOf { it.getLeft() }
        val groupWidth = group.maxOf { it.getLeft() + it.getWidth() } - groupX
        val groupY = group.minOf { it.getTop() }
        val groupHeight = group.maxOf { it.getTop() + it.getHeight() } - groupY
        NodeScannerUI.instance.showRowBounds(groupX, groupY, groupWidth, groupHeight)
    }

    private fun highlightNode(groupIndex: Int, nodeIndex: Int) {
        groups.getOrNull(groupIndex)?.getOrNull(nodeIndex)?.highlight()
    }

    /**
     * This function unhighlights the item or specific node
     * @param groupIndex The index of the group to unhighlight, or null to unhighlight the entire item
     * @param nodeIndex The index of the node within the group to unhighlight, or null to unhighlight the entire group
     */
    fun unhighlight(groupIndex: Int? = null, nodeIndex: Int? = null) {
        when {
            groupIndex == null && nodeIndex == null -> NodeScannerUI.instance.hideAll()
            groupIndex != null && nodeIndex != null -> groups.getOrNull(groupIndex)
                ?.getOrNull(nodeIndex)?.unhighlight()

            groupIndex != null && nodeIndex == null -> NodeScannerUI.instance.hideAll()

            else -> throw IllegalArgumentException("Invalid unhighlight parameters")
        }
    }

    fun getX(): Int = children.minOf { it.getLeft() }
    fun getWidth(): Int = children.maxOf { it.getLeft() + it.getWidth() } - getX()
    fun getHeight(): Int = children.maxOf { it.getTop() + it.getHeight() } - y

    fun getGroupCount(): Int = groups.size
    fun getNodeCount(groupIndex: Int): Int = groups.getOrNull(groupIndex)?.size ?: 0

    fun selectNode(groupIndex: Int, nodeIndex: Int) {
        groups.getOrNull(groupIndex)?.getOrNull(nodeIndex)?.select()
    }

    fun isSingleNode(): Boolean = children.size == 1

    fun selectSingleNodeIfApplicable(): Boolean {
        if (isSingleNode()) {
            children[0].select()
            return true
        }
        return false
    }

    fun isGrouped(): Boolean = groups.size > 1

    /**
     * This function splits the children into groups
     * If group scanning is enabled and there are 4 or more nodes, it splits the row in half
     * Otherwise, it creates a single group with all nodes
     * @param nodes The list of nodes to split into groups
     * @return A list of groups, where each group is a list of nodes
     */
    private fun splitIntoGroups(nodes: List<ScanNodeInterface>): List<List<ScanNodeInterface>> {
        val sortedNodes = nodes.sortedBy { it.getLeft() }
        return if (isGroupScanEnabled && sortedNodes.size >= 4) {
            val midpoint = sortedNodes.size / 2
            listOf(
                sortedNodes.subList(0, midpoint),
                sortedNodes.subList(midpoint, sortedNodes.size)
            )
        } else {
            listOf(sortedNodes)
        }
    }
}