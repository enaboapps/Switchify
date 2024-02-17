package com.enaboapps.switchify.service.scanning

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.preferences.PreferenceManager

class ScanTree(context: Context) : ScanStateInterface {
    /**
     * This property represents the scanning tree
     */
    private var tree: MutableList<List<ScanNodeInterface>> = mutableListOf()

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
     * This property indicates the scanning direction
     * Rows can scan up or down
     * Columns can scan left or right
     */
    private var scanDirection = ScanDirection.DOWN

    /**
     * This property indicates the state of the scanning
     */
    private var scanState = ScanState.STOPPED

    /**
     * Scanning scheduler: This is for automatic scanning
     */
    private var scanningScheduler = ScanningScheduler { stepAutoScanning() }

    private val preferenceManager = PreferenceManager(context)


    /**
     * This function builds the scanning tree
     * by examining the x and y coordinates of the nodes
     * and organizing them into a tree of rows
     * @param nodes The nodes to build the tree from
     */
    fun buildTree(nodes: List<ScanNodeInterface>) {
        var currentRow = mutableListOf<ScanNodeInterface>()
        var currentY = nodes[0].getY()
        for (node in nodes) {
            if (node.getY() != currentY) {
                tree.add(currentRow)
                currentRow = mutableListOf()
                currentY = node.getY()
            }
            currentRow.add(node)
            println("currentRow: $currentRow")
        }
        tree.add(currentRow) // Add the last row
        println("scanning tree: $tree")
    }

    /**
     * This function moves the selection to the next node
     */
    private fun moveSelectionToNextNode() {
        unhighlightCurrentNode()
        if (currentColumn < tree[currentRow].size - 1) {
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
        if (currentColumn > 0) {
            currentColumn--
        } else {
            currentColumn = tree[currentRow].size - 1
        }
        highlightCurrentNode()
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
            for (node in tree[currentRow]) {
                node.highlight()
            }
        }
    }

    /**
     * This function unhighlights the current row
     */
    private fun unhighlightCurrentRow() {
        if (tree.size > currentRow) {
            for (node in tree[currentRow]) {
                node.unhighlight()
            }
        }
    }

    /**
     * This function highlights the current node
     */
    private fun highlightCurrentNode() {
        if (tree.isNotEmpty()) {
            tree[currentRow][currentColumn].highlight()
        }
    }

    /**
     * This function unhighlights the current node
     */
    private fun unhighlightCurrentNode() {
        if (tree.isNotEmpty()) {
            tree[currentRow][currentColumn].unhighlight()
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
        if (tree[currentRow].size == 1) {
            tree[currentRow][0].select()
            return
        }
        isInRow = true
        currentColumn = 0
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
            if (tree[currentRow].size > currentColumn) {
                tree[currentRow][currentColumn].select()
            }
        }
    }

    /**
     * This function performs the selection
     * If the scanning tree is in a row, it selects the current node
     * If the scanning tree is not in a row, it selects the current row
     */
    fun performSelection() {
        if (scanState == ScanState.STOPPED) {
            startScanning()
            return
        }
        if (isInRow) {
            selectCurrentColumn()
            stopScanning()
            reset()
        } else {
            selectCurrentRow()
        }
    }

    /**
     * This function steps through the scanning tree
     */
    private fun stepAutoScanning() {
        if (scanState == ScanState.SCANNING) {
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

        Log.d("ScanTree", "stepAutoScanning scanState: $scanState")
    }

    /**
     * This function is for manually scanning forward in the scanning tree
     */
    fun stepForward() {
        scanState = ScanState.SCANNING
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
        scanState = ScanState.SCANNING
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
        val mode =
            ScanMode.fromId(preferenceManager.getIntegerValue(PreferenceManager.PREFERENCE_KEY_SCAN_MODE))
        if (scanState == ScanState.STOPPED) {
            reset()
            highlightCurrentRow() // Highlight the first row
            scanState = ScanState.SCANNING
            Log.d("ScanTree", "mode: $mode")
            if (mode.id == ScanMode.Modes.MODE_AUTO) {
                Log.d("ScanTree", "startScanning")
                val rate =
                    preferenceManager.getLongValue(PreferenceManager.PREFERENCE_KEY_SCAN_RATE)
                scanningScheduler.startScanning(rate, rate)
            }
        }
    }

    override fun pauseScanning() {
        if (scanState == ScanState.SCANNING) {
            scanningScheduler.pauseScanning()
            scanState = ScanState.PAUSED
        }
    }

    override fun resumeScanning() {
        if (scanState == ScanState.PAUSED) {
            scanningScheduler.resumeScanning()
            scanState = ScanState.SCANNING
        }
    }

    override fun stopScanning() {
        if (scanState == ScanState.SCANNING || scanState == ScanState.PAUSED) {
            scanningScheduler.stopScanning()
            scanState = ScanState.STOPPED
        }
    }

    /**
     * This function resets the scanning tree
     */
    private fun reset() {
        unhighlightCurrentNode()
        unhighlightCurrentRow()
        currentRow = 0
        currentColumn = 0
        isInRow = false
        scanDirection = ScanDirection.DOWN
    }

    /**
     * This function clears the scanning tree
     */
    fun clearTree() {
        tree.clear()
    }
}