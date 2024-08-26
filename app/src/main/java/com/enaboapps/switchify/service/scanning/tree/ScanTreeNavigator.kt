package com.enaboapps.switchify.service.scanning.tree

import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanSettings

/**
 * This class is responsible for navigating through the ScanTree structure.
 * It manages the current position and provides methods for moving between items, groups, and nodes.
 *
 * @property tree The list of ScanTreeItems that make up the scanning tree.
 * @property scanSettings The settings for scanning behavior.
 */
class ScanTreeNavigator(
    private val tree: List<ScanTreeItem>,
    private val scanSettings: ScanSettings
) {
    /** The index of the current tree item being scanned. */
    var currentTreeItem = 0

    /** The index of the current group within the current tree item. */
    var currentGroup = 0

    /** The index of the current column within the current group. */
    var currentColumn = 0

    /** Indicates whether the scanning is currently within a tree item. */
    var isInTreeItem = false

    /** The current direction of scanning. */
    var scanDirection = ScanDirection.DOWN

    /** Indicates whether the current item should be escaped. */
    var shouldEscapeItem = false

    /**
     * Moves the selection to the next element based on the current state and settings.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    fun moveSelectionToNext(): Boolean {
        return when {
            !isInTreeItem -> moveSelectionToNextTreeItem()
            scanDirection == ScanDirection.RIGHT -> moveSelectionToNextWithinItem()
            else -> false // This should not happen in normal operation
        }
    }

    /**
     * Moves the selection to the previous element based on the current state and settings.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    fun moveSelectionToPrevious(): Boolean {
        return when {
            !isInTreeItem -> moveSelectionToPreviousTreeItem()
            scanDirection == ScanDirection.LEFT -> moveSelectionToPreviousWithinItem()
            else -> false // This should not happen in normal operation
        }
    }

    /**
     * Moves the selection to the next element within the current item.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    private fun moveSelectionToNextWithinItem(): Boolean {
        val currentItem = getCurrentItem()
        return when {
            currentColumn < currentItem.getNodeCount(currentGroup) - 1 -> {
                currentColumn++
                true
            }

            currentGroup < currentItem.getGroupCount() - 1 -> {
                currentGroup++
                currentColumn = 0
                true
            }

            else -> {
                shouldEscapeItem = true
                false
            }
        }
    }

    /**
     * Moves the selection to the previous element within the current item.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    private fun moveSelectionToPreviousWithinItem(): Boolean {
        return when {
            currentColumn > 0 -> {
                currentColumn--
                true
            }

            currentGroup > 0 -> {
                currentGroup--
                currentColumn = getCurrentItem().getNodeCount(currentGroup) - 1
                true
            }

            else -> {
                shouldEscapeItem = true
                false
            }
        }
    }

    /**
     * Moves the selection to the next tree item.
     * @return Always returns true as it wraps around to the first item if at the end.
     */
    private fun moveSelectionToNextTreeItem(): Boolean {
        currentTreeItem = if (currentTreeItem < tree.size - 1) currentTreeItem + 1 else 0
        resetGroupAndColumn()
        return true
    }

    /**
     * Moves the selection to the previous tree item.
     * @return Always returns true as it wraps around to the last item if at the beginning.
     */
    private fun moveSelectionToPreviousTreeItem(): Boolean {
        currentTreeItem = if (currentTreeItem > 0) currentTreeItem - 1 else tree.size - 1
        resetGroupAndColumn()
        return true
    }

    /**
     * Resets the group and column indices to their initial values.
     */
    private fun resetGroupAndColumn() {
        currentGroup = 0
        currentColumn = 0
    }

    /**
     * Handles the escape logic for items.
     * @return True if an escape was handled, false otherwise.
     */
    fun handleEscape(): Boolean = shouldEscapeItem

    /**
     * Confirms the escape action and updates the navigation state accordingly.
     */
    fun confirmEscape() {
        if (shouldEscapeItem) {
            isInTreeItem = false
            if (scanDirection == ScanDirection.DOWN) {
                moveSelectionToNextTreeItem()
            } else {
                moveSelectionToPreviousTreeItem()
            }
            shouldEscapeItem = false
        }
    }

    /**
     * Denies the escape action and resets the escape flag.
     */
    fun denyEscape() {
        if (shouldEscapeItem) {
            // Move to the appropriate edge of the current item based on scan direction
            if (scanDirection == ScanDirection.RIGHT) {
                currentGroup = 0
                currentColumn = 0
            } else {
                currentGroup = getCurrentItem().getGroupCount() - 1
                currentColumn = getCurrentItem().getNodeCount(currentGroup) - 1
            }
            shouldEscapeItem = false
        }
    }

    /**
     * Gets the current ScanTreeItem.
     * @return The current ScanTreeItem.
     */
    fun getCurrentItem(): ScanTreeItem = tree[currentTreeItem]

    /**
     * Swaps the scanning direction between vertical and horizontal.
     */
    fun swapScanDirection() {
        scanDirection = when (scanDirection) {
            ScanDirection.DOWN -> ScanDirection.UP
            ScanDirection.UP -> ScanDirection.DOWN
            ScanDirection.RIGHT -> ScanDirection.LEFT
            ScanDirection.LEFT -> ScanDirection.RIGHT
        }
    }

    /**
     * Resets the navigator to its initial state.
     */
    fun reset() {
        currentTreeItem = 0
        currentGroup = 0
        currentColumn = 0
        isInTreeItem = false
        shouldEscapeItem = false
        scanDirection = ScanDirection.DOWN
    }
}