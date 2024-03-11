package com.enaboapps.switchify.service.scanning.tree

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.math.abs

/**
 * This class represents the scanning tree
 * @param context The context
 * @param stopScanningOnSelect Whether to stop scanning on select
 * @param individualHighlightingItemsInTreeItem Whether to highlight items individually in an item
 */

class ScanTree(
    private val context: Context,
    private var stopScanningOnSelect: Boolean = false,
    private var individualHighlightingItemsInTreeItem: Boolean = true
) : ScanStateInterface {
    /**
     * This property represents the scanning tree
     */
    private var tree: MutableList<ScanTreeItem> = mutableListOf()

    /**
     * This property indicates the current item of the scanning tree
     */
    private var currentTreeItem = 0

    /**
     * This property indicates the current column of the scanning tree
     */
    private var currentColumn = 0

    /**
     * This property indicates whether the scanning tree is in an item
     */
    private var isInTreeItem = false

    /**
     * This property indicates whether the current item should be escaped
     */
    private var shouldEscapeTreeItem = false

    /**
     * This property indicates the scanning direction
     * TreeItems can scan up or down
     * Columns can scan left or right
     */
    private var scanDirection = ScanDirection.DOWN

    /**
     * Scanning scheduler: This is for automatic scanning
     */
    private var scanningScheduler: ScanningScheduler? = null

    /**
     * Scan settings
     */
    private val scanSettings = ScanSettings(context)


    /**
     * This function builds the scanning tree
     * by examining the x and y coordinates of the nodes
     * and organizing them into a tree of items and columns
     * @param nodes The nodes to build the tree from
     * @param itemThreshold The threshold for determining if a node is in an item
     */
    fun buildTree(nodes: List<ScanNodeInterface>, itemThreshold: Int = 40) {
        reset()
        clearTree()
        if (nodes.isNotEmpty()) {
            // Initial sort of nodes by their Y position to process from top to bottom.
            val sortedNodes = nodes.sortedBy { it.getY() }

            // Initialize the list for the first item with the first node.
            var currentTreeItem = mutableListOf<ScanNodeInterface>(sortedNodes.first())
            // Set the Y position baseline for the first item.
            var currentYBaseline = sortedNodes.first().getY()

            // Get screen dimensions.
            val screenWidth = ScreenUtils.getWidth(context)
            val screenHeight = ScreenUtils.getHeight(context)

            // Function to add the node to the current item.
            val addNodeToTreeItem: (ScanNodeInterface) -> Unit = { node ->
                // If node is under 80% of the screen dimensions
                // And larger than 0, add it to the current item
                val width = node.getWidth()
                val height = node.getHeight()
                val isCloseToFullScreen = width > 0.8 * screenWidth && height > 0.8 * screenHeight
                val isBiggerThanZero = width > 0 && height > 0
                if (!isCloseToFullScreen && isBiggerThanZero) {
                    currentTreeItem.add(node)
                }
            }

            // Start iterating from the second node since the first is already included.
            for (node in sortedNodes.drop(1)) {
                // Determine if the current node's Y position is within the threshold of the current item's baseline.
                if (abs(node.getY() - currentYBaseline) <= ScreenUtils.dpToPx(
                        context,
                        itemThreshold
                    )
                ) {
                    // Node is close enough to be considered part of the current item.
                    addNodeToTreeItem(node)
                } else {
                    // Node is too far from the current item's baseline, indicating a new item.
                    // Process the current item before starting a new one.
                    if (currentTreeItem.isNotEmpty()) {
                        // Remove duplicates from the item.
                        currentTreeItem = currentTreeItem.distinct().toMutableList()
                        // Add the current item to the scanning tree.
                        addItem(currentTreeItem)
                        // Clear the current item for the next iteration.
                        currentTreeItem = mutableListOf()
                    }
                    // Add the current node to the new item and update the baseline Y position.
                    addNodeToTreeItem(node)
                    currentYBaseline = node.getY()
                }
            }

            // Ensure the last item is added after processing all nodes.
            if (currentTreeItem.isNotEmpty()) {
                addItem(currentTreeItem)
            }
        }
    }

    /**
     * This function adds an item to the scanning tree sorted ascending by the x coordinate
     * @param children The children to add
     */
    private fun addItem(children: List<ScanNodeInterface>) {
        if (children.isNotEmpty()) {
            val sorted = children.sortedBy { it.getX() }
            if (scanSettings.isRowColumnScanEnabled()) {
                val item =
                    ScanTreeItem(sorted, sorted[0].getY(), individualHighlightingItemsInTreeItem)
                tree.add(item)
            } else {
                sorted.forEach {
                    val item =
                        ScanTreeItem(listOf(it), it.getY(), individualHighlightingItemsInTreeItem)
                    tree.add(item)
                }
            }
        }
    }

    /**
     * This function shuts down the scanning scheduler
     */
    fun shutdown() {
        scanningScheduler?.shutdown()
        scanningScheduler = null
    }

    /**
     * This function sets the scanning scheduler
     */
    fun setup() {
        reset()

        if (scanningScheduler == null) {
            scanningScheduler = ScanningScheduler(context) {
                stepAutoScanning()
            }
        }
    }

    /**
     * This function moves the selection to the next node
     */
    private fun moveSelectionToNextNode() {
        unhighlightCurrentNode()
        if (shouldEscapeCurrentTreeItem()) {
            return
        }
        if (currentColumn < tree[currentTreeItem].children.size - 1) {
            currentColumn++
        } else {
            currentColumn = 0
        }
        highlightCurrentNode()
    }

    /**
     * This function moves the selection to the previous node
     */
    private fun moveSelectionToPreviousNode() {
        unhighlightCurrentNode()
        if (shouldEscapeCurrentTreeItem()) {
            return
        }
        if (currentColumn > 0) {
            currentColumn--
        } else {
            currentColumn = tree[currentTreeItem].children.size - 1
        }
        highlightCurrentNode()
    }

    /**
     * This function checks if the current item should be escaped
     * @return Whether the current item should be escaped
     */
    private fun shouldEscapeCurrentTreeItem(): Boolean {
        // If at the last node, activate the escape item
        if (currentColumn == tree[currentTreeItem].children.size - 1 && !shouldEscapeTreeItem && scanDirection == ScanDirection.RIGHT) {
            shouldEscapeTreeItem = true
            highlightCurrentTreeItem()

            return true
        } else if (currentColumn == 0 && !shouldEscapeTreeItem && scanDirection == ScanDirection.LEFT) {
            shouldEscapeTreeItem = true
            highlightCurrentTreeItem()

            return true
        } else if (shouldEscapeTreeItem) {
            shouldEscapeTreeItem = false
            unhighlightCurrentTreeItem()

            // Ensure that the index is correct
            currentColumn = if (scanDirection == ScanDirection.RIGHT) {
                0
            } else {
                tree[currentTreeItem].children.size - 1
            }
            highlightCurrentNode()

            return true
        }
        return false
    }

    /**
     * This function moves the selection to the next item
     */
    private fun moveSelectionToNextTreeItem() {
        unhighlightCurrentTreeItem()
        if (currentTreeItem < tree.size - 1) {
            currentTreeItem++
        } else {
            currentTreeItem = 0
        }
        highlightCurrentTreeItem()
    }

    /**
     * This function moves the selection to the previous item
     */
    private fun moveSelectionToPreviousTreeItem() {
        unhighlightCurrentTreeItem()
        if (currentTreeItem > 0) {
            currentTreeItem--
        } else {
            currentTreeItem = tree.size - 1
        }
        highlightCurrentTreeItem()
    }

    /**
     * This function highlights the current item
     */
    private fun highlightCurrentTreeItem() {
        if (tree.size > currentTreeItem) {
            tree[currentTreeItem].highlight()
        }
    }

    /**
     * This function unhighlights the current item
     */
    private fun unhighlightCurrentTreeItem() {
        if (tree.size > currentTreeItem) {
            tree[currentTreeItem].unhighlight()
        }
    }

    /**
     * This function highlights the current node
     */
    private fun highlightCurrentNode() {
        if (tree.size > currentTreeItem) {
            if (tree[currentTreeItem].children.size > currentColumn) {
                tree[currentTreeItem].children[currentColumn].highlight()
            }
        }
    }

    /**
     * This function unhighlights the current node
     */
    private fun unhighlightCurrentNode() {
        if (tree.size > currentTreeItem) {
            if (tree[currentTreeItem].children.size > currentColumn) {
                tree[currentTreeItem].children[currentColumn].unhighlight()
            }
        }
    }

    /**
     * This function swaps the scanning direction
     */
    fun swapScanDirection() {
        scanDirection = when (scanDirection) {
            ScanDirection.DOWN -> ScanDirection.UP
            ScanDirection.UP -> ScanDirection.DOWN
            ScanDirection.RIGHT -> ScanDirection.LEFT
            ScanDirection.LEFT -> ScanDirection.RIGHT
        }

        if (scanSettings.isAutoScanMode()) {
            resumeScanning()
        }
    }

    /**
     * This function selects the current item
     * If the current item has only one node, it selects the node and returns
     * It sets the scanning tree to be in an item
     * It sets the current column to 0
     * It unhighlights the current item
     * It highlights the current node
     * It pauses scanning
     * It resumes scanning
     */
    private fun selectCurrentTreeItem() {
        if (tree.size > currentTreeItem) {
            if (tree[currentTreeItem].children.size == 1) {
                tree[currentTreeItem].children[0].select()
                if (stopScanningOnSelect) {
                    reset()
                }
                return
            }
        }
        isInTreeItem = true
        currentColumn = 0
        scanDirection = ScanDirection.RIGHT
        unhighlightCurrentTreeItem()
        highlightCurrentNode()
        pauseScanning()
        resumeScanning()
    }

    /**
     * This function selects the current column
     */
    private fun selectCurrentColumn() {
        // Check if the item exists
        if (tree.size > currentTreeItem) {
            // Check if the column exists
            if (tree[currentTreeItem].children.size > currentColumn) {
                tree[currentTreeItem].children[currentColumn].select()
                if (stopScanningOnSelect) {
                    reset()
                }
            }
        }
    }

    /**
     * This function performs the selection
     * It starts scanning if it is not already scanning
     * It escapes the item if the item should be escaped
     * If the scanning tree is in an item, it selects the current node
     * If the scanning tree is not in an item, it selects the current item
     */
    fun performSelection() {
        try {
            if (scanningScheduler?.isScanning() == false) {
                startScanning()
                println("Scanning started")
                return
            }
            if (shouldEscapeTreeItem) {
                shouldEscapeTreeItem = false
                unhighlightCurrentTreeItem()
                reset()
                return
            }
            if (isInTreeItem) {
                selectCurrentColumn()
            } else {
                selectCurrentTreeItem()
            }
        } catch (e: Exception) {
            println("Error performing selection: ${e.message}")
        }
    }

    /**
     * This function steps through the scanning tree
     */
    private fun stepAutoScanning() {
        if (isInTreeItem) {
            if (scanDirection == ScanDirection.RIGHT) {
                moveSelectionToNextNode()
            } else {
                moveSelectionToPreviousNode()
            }
        } else {
            if (scanDirection == ScanDirection.DOWN) {
                moveSelectionToNextTreeItem()
            } else {
                moveSelectionToPreviousTreeItem()
            }
        }
    }

    /**
     * This function is for manually scanning forward in the scanning tree
     */
    fun stepForward() {
        if (isInTreeItem) {
            moveSelectionToNextNode()
        } else {
            moveSelectionToNextTreeItem()
        }
    }

    /**
     * This function is for manually scanning backward in the scanning tree
     */
    fun stepBackward() {
        if (isInTreeItem) {
            moveSelectionToPreviousNode()
        } else {
            moveSelectionToPreviousTreeItem()
        }
    }

    /**
     * This function starts scanning
     */
    private fun startScanning() {
        if (tree.isNotEmpty()) {
            reset()
            highlightCurrentTreeItem() // Highlight the first item
            if (scanSettings.isAutoScanMode()) {
                Log.d("ScanTree", "startScanning")
                scanningScheduler?.startScanning()
            }
        }
    }

    override fun pauseScanning() {
        scanningScheduler?.pauseScanning()
    }

    override fun resumeScanning() {
        scanningScheduler?.resumeScanning()
    }

    override fun stopScanning() {
        scanningScheduler?.stopScanning()
    }

    /**
     * This function resets the scanning tree
     */
    fun reset() {
        try {
            for (item in tree) {
                item.unhighlight()
                for (node in item.children) {
                    node.unhighlight()
                }
            }
        } catch (e: Exception) {
            println("Error resetting scanning tree: ${e.message}")
        }
        currentTreeItem = 0
        currentColumn = 0
        isInTreeItem = false
        scanDirection = ScanDirection.DOWN
        stopScanning()
    }

    /**
     * This function clears the scanning tree
     */
    fun clearTree() {
        tree.clear()
    }
}