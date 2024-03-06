package com.enaboapps.switchify.service.scanning

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlin.math.abs

class ScanTree(
    private val context: Context,
    private var stopScanningOnSelect: Boolean = false
) : ScanStateInterface {
    /**
     * This property represents the scanning tree
     */
    private var tree: MutableList<Row> = mutableListOf()

    /**
     * This property indicates the current row of the scanning tree
     */
    private var currentRow = 0

    /**
     * This property indicates the current column of the scanning tree
     */
    private var currentColumn = 0

    /**
     * This property indicates whether the scanning tree is in a row
     */
    private var isInRow = false

    /**
     * This property indicates whether the current row should be escaped
     */
    private var shouldEscapeRow = false

    /**
     * This property indicates the scanning direction
     * Rows can scan up or down
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
     * Data class representing a row
     * @param nodes The nodes in the row
     * @param y The y coordinate of the row
     */
    private data class Row(val nodes: List<ScanNodeInterface>, val y: Int)


    /**
     * This function builds the scanning tree
     * by examining the x and y coordinates of the nodes
     * and organizing them into a tree of rows and columns
     * @param nodes The nodes to build the tree from
     * @param rowThreshold The threshold for determining if a node is in a row
     */
    fun buildTree(nodes: List<ScanNodeInterface>, rowThreshold: Int = 100) {
        reset()
        clearTree()
        if (nodes.isNotEmpty()) {
            // Initial sort of nodes by their Y position to process from top to bottom.
            val sortedNodes = nodes.sortedBy { it.getY() }

            // Initialize the list for the first row with the first node.
            var currentRow = mutableListOf<ScanNodeInterface>(sortedNodes.first())
            // Set the Y position baseline for the first row.
            var currentYBaseline = sortedNodes.first().getY()

            // Get screen dimensions.
            val screenWidth = ScreenUtils.getWidth(context)
            val screenHeight = ScreenUtils.getHeight(context)

            // Function to add the node to the current row.
            val addNodeToRow: (ScanNodeInterface) -> Unit = { node ->
                // If node is under 80% of the screen dimensions
                // And larger than 0, add it to the current row
                val width = node.getWidth()
                val height = node.getHeight()
                val isCloseToFullScreen = width > 0.8 * screenWidth && height > 0.8 * screenHeight
                val isBiggerThanZero = width > 0 && height > 0
                if (!isCloseToFullScreen && isBiggerThanZero) {
                    currentRow.add(node)
                }
            }

            // Start iterating from the second node since the first is already included.
            for (node in sortedNodes.drop(1)) {
                // Determine if the current node's Y position is within the threshold of the current row's baseline.
                if (abs(node.getY() - currentYBaseline) <= rowThreshold) {
                    // Node is close enough to be considered part of the current row.
                    addNodeToRow(node)
                } else {
                    // Node is too far from the current row's baseline, indicating a new row.
                    // Process the current row before starting a new one.
                    if (currentRow.isNotEmpty()) {
                        // Remove duplicates from the row.
                        currentRow = currentRow.distinct().toMutableList()
                        // Add the current row to the scanning tree.
                        addRow(currentRow)
                        // Clear the current row for the next iteration.
                        currentRow = mutableListOf()
                    }
                    // Add the current node to the new row and update the baseline Y position.
                    addNodeToRow(node)
                    currentYBaseline = node.getY()
                }
            }

            // Ensure the last row is added after processing all nodes.
            if (currentRow.isNotEmpty()) {
                addRow(currentRow)
            }
        }

        setupScanningScheduler()
    }

    /**
     * This function adds a row to the scanning tree sorted ascending by the x coordinate
     * @param row The row to add
     */
    private fun addRow(row: List<ScanNodeInterface>) {
        if (row.isNotEmpty()) {
            val sortedRow = row.sortedBy { it.getX() }
            tree.add(Row(sortedRow, sortedRow[0].getY()))
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
    private fun setupScanningScheduler() {
        reset()
        shutdown()

        scanningScheduler = ScanningScheduler(context) { stepAutoScanning() }
    }

    /**
     * This function moves the selection to the next node
     */
    private fun moveSelectionToNextNode() {
        unhighlightCurrentNode()
        if (shouldEscapeCurrentRow()) {
            return
        }
        if (currentColumn < tree[currentRow].nodes.size - 1) {
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
        if (shouldEscapeCurrentRow()) {
            return
        }
        if (currentColumn > 0) {
            currentColumn--
        } else {
            currentColumn = tree[currentRow].nodes.size - 1
        }
        highlightCurrentNode()
    }

    /**
     * This function checks if escaping the current row is necessary
     * @return True if escaping the current row is necessary, false otherwise
     */
    private fun shouldEscapeCurrentRow(): Boolean {
        // If at the last node, activate the escape row
        if (currentColumn == tree[currentRow].nodes.size - 1 && !shouldEscapeRow && scanDirection == ScanDirection.RIGHT) {
            shouldEscapeRow = true
            highlightCurrentRow()
        } else if (currentColumn == 0 && !shouldEscapeRow && scanDirection == ScanDirection.LEFT) {
            shouldEscapeRow = true
            highlightCurrentRow()
        } else if (shouldEscapeRow) {
            shouldEscapeRow = false
            unhighlightCurrentRow()
        }
        return shouldEscapeRow
    }

    /**
     * This function moves the selection to the next row
     */
    private fun moveSelectionToNextRow() {
        unhighlightCurrentRow()
        if (currentRow < tree.size - 1) {
            currentRow++
        } else {
            currentRow = 0
        }
        highlightCurrentRow()
    }

    /**
     * This function moves the selection to the previous row
     */
    private fun moveSelectionToPreviousRow() {
        unhighlightCurrentRow()
        if (currentRow > 0) {
            currentRow--
        } else {
            currentRow = tree.size - 1
        }
        highlightCurrentRow()
    }

    /**
     * This function highlights the current row
     */
    private fun highlightCurrentRow() {
        if (tree.size > currentRow) {
            for (node in tree[currentRow].nodes) {
                node.highlight()
            }
        }
    }

    /**
     * This function unhighlights the current row
     */
    private fun unhighlightCurrentRow() {
        if (tree.size > currentRow) {
            for (node in tree[currentRow].nodes) {
                node.unhighlight()
            }
        }
    }

    /**
     * This function highlights the current node
     */
    private fun highlightCurrentNode() {
        if (tree.size > currentRow) {
            if (tree[currentRow].nodes.size > currentColumn) {
                tree[currentRow].nodes[currentColumn].highlight()
            }
        }
    }

    /**
     * This function unhighlights the current node
     */
    private fun unhighlightCurrentNode() {
        if (tree.size > currentRow) {
            if (tree[currentRow].nodes.size > currentColumn) {
                tree[currentRow].nodes[currentColumn].unhighlight()
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
     * This function selects the current row
     * If the current row has only one node, it selects the node and returns
     * It sets the scanning tree to be in a row
     * It sets the current column to 0
     * It unhighlights the current row
     * It highlights the current node
     * It pauses scanning
     * It resumes scanning
     */
    private fun selectCurrentRow() {
        if (tree.size > currentRow) {
            if (tree[currentRow].nodes.size == 1) {
                tree[currentRow].nodes[0].select()
                if (stopScanningOnSelect) {
                    reset()
                }
                return
            }
        }
        isInRow = true
        currentColumn = 0
        scanDirection = ScanDirection.RIGHT
        unhighlightCurrentRow()
        highlightCurrentNode()
        pauseScanning()
        resumeScanning()
    }

    /**
     * This function selects the current column
     */
    private fun selectCurrentColumn() {
        // Check if the row exists
        if (tree.size > currentRow) {
            // Check if the column exists
            if (tree[currentRow].nodes.size > currentColumn) {
                tree[currentRow].nodes[currentColumn].select()
                if (stopScanningOnSelect) {
                    reset()
                }
            }
        }
    }

    /**
     * This function performs the selection
     * It starts scanning if it is not already scanning
     * It escapes the row if the row should be escaped
     * If the scanning tree is in a row, it selects the current node
     * If the scanning tree is not in a row, it selects the current row
     */
    fun performSelection() {
        try {
            if (scanningScheduler?.isScanning() == false) {
                startScanning()
                println("Scanning started")
                return
            }
            if (shouldEscapeRow) {
                shouldEscapeRow = false
                unhighlightCurrentRow()
                reset()
                return
            }
            if (isInRow) {
                selectCurrentColumn()
            } else {
                selectCurrentRow()
            }
        } catch (e: Exception) {
            println("Error performing selection: ${e.message}")
        }
    }

    /**
     * This function steps through the scanning tree
     */
    private fun stepAutoScanning() {
        if (isInRow) {
            if (scanDirection == ScanDirection.RIGHT) {
                moveSelectionToNextNode()
            } else {
                moveSelectionToPreviousNode()
            }
        } else {
            if (scanDirection == ScanDirection.DOWN) {
                moveSelectionToNextRow()
            } else {
                moveSelectionToPreviousRow()
            }
        }
    }

    /**
     * This function is for manually scanning forward in the scanning tree
     */
    fun stepForward() {
        if (isInRow) {
            moveSelectionToNextNode()
        } else {
            moveSelectionToNextRow()
        }
    }

    /**
     * This function is for manually scanning backward in the scanning tree
     */
    fun stepBackward() {
        if (isInRow) {
            moveSelectionToPreviousNode()
        } else {
            moveSelectionToPreviousRow()
        }
    }

    /**
     * This function starts scanning
     */
    private fun startScanning() {
        if (tree.isNotEmpty()) {
            reset()
            highlightCurrentRow() // Highlight the first row
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
            for (row in tree) {
                for (node in row.nodes) {
                    node.unhighlight()
                }
            }
        } catch (e: Exception) {
            println("Error resetting scanning tree: ${e.message}")
        }
        currentRow = 0
        currentColumn = 0
        isInRow = false
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