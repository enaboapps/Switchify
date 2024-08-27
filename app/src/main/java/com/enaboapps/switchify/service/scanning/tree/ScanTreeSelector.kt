package com.enaboapps.switchify.service.scanning.tree

import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanSettings

/**
 * This class is responsible for handling the selection logic in the ScanTree structure.
 * It works in conjunction with ScanTreeNavigator to perform selections based on the current state.
 *
 * @property tree The list of ScanTreeItems that make up the scanning tree.
 * @property navigator The ScanTreeNavigator used for traversing the tree.
 * @property scanSettings The settings for scanning behavior.
 * @property stopScanningOnSelect Whether to stop scanning after a selection is made.
 */
class ScanTreeSelector(
    private val tree: List<ScanTreeItem>,
    private val navigator: ScanTreeNavigator,
    private val scanSettings: ScanSettings,
    private val stopScanningOnSelect: Boolean
) {
    /**
     * Performs the selection action based on the current scanning state.
     * @return True if a selection was made, false otherwise.
     */
    fun performSelection(): Boolean {
        return when {
            !navigator.isInTreeItem -> selectCurrentTreeItem()
            scanSettings.isGroupScanEnabled() && getCurrentItem().isGrouped() && navigator.currentColumn == 0 -> selectCurrentGroup()
            else -> selectCurrentNode()
        }
    }

    /**
     * Selects the current tree item.
     * @return True if a selection was made, false otherwise.
     */
    private fun selectCurrentTreeItem(): Boolean {
        val currentItem = getCurrentItem()
        if (currentItem.isSingleNode()) {
            currentItem.selectSingleNodeIfApplicable()
            if (stopScanningOnSelect) {
                navigator.reset()
            }
            return true
        }
        navigator.isInTreeItem = true
        navigator.currentGroup = 0
        navigator.currentColumn = 0
        navigator.scanDirection = ScanDirection.RIGHT
        return false
    }

    /**
     * Selects the current group within the current tree item.
     * @return True if a selection was made, false otherwise.
     */
    private fun selectCurrentGroup(): Boolean {
        navigator.currentColumn = 0
        navigator.scanDirection = ScanDirection.RIGHT
        navigator.isScanningGroups = false
        return false
    }

    /**
     * Selects the current node within the current group.
     * @return True if a selection was made, false otherwise.
     */
    private fun selectCurrentNode(): Boolean {
        getCurrentItem().selectNode(navigator.currentGroup, navigator.currentColumn)
        if (stopScanningOnSelect) {
            navigator.reset()
        }
        return true
    }

    /**
     * Gets the current ScanTreeItem.
     * @return The current ScanTreeItem.
     */
    private fun getCurrentItem(): ScanTreeItem {
        return tree[navigator.currentTreeItem]
    }
}