package com.enaboapps.switchify.service.scanning.tree

import com.enaboapps.switchify.service.scanning.ScanSettings

/**
 * This class is responsible for managing the visual highlighting of elements in the ScanTree structure.
 * It provides methods to highlight and unhighlight tree items, groups, and individual nodes.
 *
 * @property tree The list of ScanTreeItems that make up the scanning tree.
 * @property scanSettings The settings for scanning behavior.
 */
class ScanTreeHighlighter(
    private val tree: List<ScanTreeItem>,
    private val scanSettings: ScanSettings
) {
    /**
     * Highlights the current item or group to indicate an escape step.
     *
     * @param treeItemIndex The index of the current tree item.
     * @param groupIndex The index of the current group within the tree item.
     * @param isInTreeItem Whether the scanning is currently within a tree item.
     */
    fun highlightEscape(
        treeItemIndex: Int,
        groupIndex: Int,
        isInTreeItem: Boolean
    ) {
        val currentItem = tree.getOrNull(treeItemIndex) ?: return

        if (!isInTreeItem) {
            highlightTreeItem(currentItem)
        } else {
            highlightGroup(currentItem, groupIndex)
        }
    }

    /**
     * Unhighlights the current item or group to indicate an escape step.
     *
     * @param treeItemIndex The index of the current tree item.
     * @param groupIndex The index of the current group within the tree item.
     * @param isInTreeItem Whether the scanning is currently within a tree item.
     */
    fun unhighlightEscape(
        treeItemIndex: Int,
        groupIndex: Int,
        isInTreeItem: Boolean
    ) {
        val currentItem = tree.getOrNull(treeItemIndex) ?: return

        if (!isInTreeItem) {
            unhighlightTreeItem(currentItem)
        } else {
            unhighlightGroup(currentItem, groupIndex)
        }
    }

    /**
     * Highlights the current element based on the given indices and scanning state.
     *
     * @param treeItemIndex The index of the current tree item.
     * @param groupIndex The index of the current group within the tree item.
     * @param nodeIndex The index of the current node within the group.
     * @param isInTreeItem Whether the scanning is currently within a tree item.
     */
    fun highlightCurrent(
        treeItemIndex: Int,
        groupIndex: Int,
        nodeIndex: Int,
        isInTreeItem: Boolean
    ) {
        val currentItem = tree.getOrNull(treeItemIndex) ?: return

        when {
            !isInTreeItem -> highlightTreeItem(currentItem)
            !scanSettings.isGroupScanEnabled() || !currentItem.isGrouped() ->
                highlightNode(currentItem, groupIndex, nodeIndex)

            else -> highlightGroup(currentItem, groupIndex)
        }
    }

    /**
     * Unhighlights the current element based on the given indices and scanning state.
     *
     * @param treeItemIndex The index of the current tree item.
     * @param groupIndex The index of the current group within the tree item.
     * @param nodeIndex The index of the current node within the group.
     * @param isInTreeItem Whether the scanning is currently within a tree item.
     */
    fun unhighlightCurrent(
        treeItemIndex: Int,
        groupIndex: Int,
        nodeIndex: Int,
        isInTreeItem: Boolean
    ) {
        val currentItem = tree.getOrNull(treeItemIndex) ?: return

        when {
            !isInTreeItem -> unhighlightTreeItem(currentItem)
            !scanSettings.isGroupScanEnabled() || !currentItem.isGrouped() ->
                unhighlightNode(currentItem, groupIndex, nodeIndex)

            else -> unhighlightGroup(currentItem, groupIndex)
        }
    }

    /**
     * Highlights a specific tree item.
     *
     * @param treeItemIndex The index of the tree item to highlight.
     */
    fun highlightCurrentTreeItem(treeItemIndex: Int) {
        tree.getOrNull(treeItemIndex)?.highlight()
    }

    /**
     * Unhighlights a specific tree item.
     *
     * @param treeItemIndex The index of the tree item to unhighlight.
     */
    fun unhighlightCurrentTreeItem(treeItemIndex: Int) {
        tree.getOrNull(treeItemIndex)?.unhighlight()
    }

    /**
     * Highlights a specific group within a tree item.
     *
     * @param treeItemIndex The index of the tree item containing the group.
     * @param groupIndex The index of the group to highlight.
     */
    fun highlightCurrentGroup(treeItemIndex: Int, groupIndex: Int) {
        tree.getOrNull(treeItemIndex)?.highlight(groupIndex)
    }

    /**
     * Unhighlights a specific group within a tree item.
     *
     * @param treeItemIndex The index of the tree item containing the group.
     * @param groupIndex The index of the group to unhighlight.
     */
    fun unhighlightCurrentGroup(treeItemIndex: Int, groupIndex: Int) {
        tree.getOrNull(treeItemIndex)?.unhighlight(groupIndex)
    }

    /**
     * Highlights a tree item.
     *
     * @param item The ScanTreeItem to highlight.
     */
    private fun highlightTreeItem(item: ScanTreeItem) {
        item.highlight()
    }

    /**
     * Unhighlights a tree item.
     *
     * @param item The ScanTreeItem to unhighlight.
     */
    private fun unhighlightTreeItem(item: ScanTreeItem) {
        item.unhighlight()
    }

    /**
     * Highlights a group within a tree item.
     *
     * @param item The ScanTreeItem containing the group.
     * @param groupIndex The index of the group to highlight.
     */
    private fun highlightGroup(item: ScanTreeItem, groupIndex: Int) {
        item.highlight(groupIndex)
    }

    /**
     * Unhighlights a group within a tree item.
     *
     * @param item The ScanTreeItem containing the group.
     * @param groupIndex The index of the group to unhighlight.
     */
    private fun unhighlightGroup(item: ScanTreeItem, groupIndex: Int) {
        item.unhighlight(groupIndex)
    }

    /**
     * Highlights a specific node within a tree item.
     *
     * @param item The ScanTreeItem containing the node.
     * @param groupIndex The index of the group containing the node.
     * @param nodeIndex The index of the node to highlight.
     */
    private fun highlightNode(item: ScanTreeItem, groupIndex: Int, nodeIndex: Int) {
        item.highlight(groupIndex, nodeIndex)
    }

    /**
     * Unhighlights a specific node within a tree item.
     *
     * @param item The ScanTreeItem containing the node.
     * @param groupIndex The index of the group containing the node.
     * @param nodeIndex The index of the node to unhighlight.
     */
    private fun unhighlightNode(item: ScanTreeItem, groupIndex: Int, nodeIndex: Int) {
        item.unhighlight(groupIndex, nodeIndex)
    }

    /**
     * Unhighlights all elements in the tree.
     */
    fun unhighlightAll() {
        tree.forEach { it.unhighlight() }
    }
}