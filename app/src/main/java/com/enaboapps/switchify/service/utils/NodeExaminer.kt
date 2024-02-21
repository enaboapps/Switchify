package com.enaboapps.switchify.service.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

object NodeExaminer {

    var currentRows: List<List<AccessibilityNodeInfo>> = emptyList()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var examineJob: Job? = null

    private const val ROW_TOLERANCE = 40 // pixels

    /**
     * Find rows of nodes
     * @param rootNode The root node to start from
     */
    fun findRowsOfNodes(rootNode: AccessibilityNodeInfo) {
        examineJob?.cancel()
        examineJob = coroutineScope.launch {
            val allNodes = flattenTree(rootNode)
            val rows = groupNodesIntoRows(allNodes)
            currentRows = rows
        }
    }

    /**
     * Find rows of nodes by package name
     * @param rootNode The root node to start from
     * @param packageName The package name to search for
     */
    fun findRowsOfNodesByPackageName(
        rootNode: AccessibilityNodeInfo,
        packageName: String
    ) {
        examineJob?.cancel()
        examineJob = coroutineScope.launch {
            val allNodes = flattenTree(rootNode)
            val nodesInPackage = allNodes.filter { node ->
                node.packageName.toString() == packageName
            }
            val rows = groupNodesIntoRows(nodesInPackage)
            currentRows = rows
        }
    }

    /**
     * Flatten the tree of nodes
     * @param rootNode The root node to start from
     * @return The list of nodes
     */
    private suspend fun flattenTree(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> =
        withContext(Dispatchers.IO) {
            val allNodes: MutableList<AccessibilityNodeInfo> = ArrayList()
            val q: Queue<AccessibilityNodeInfo> = LinkedList()
            q.add(rootNode)

            while (q.isNotEmpty()) {
                val node = q.poll()
                if (node != null) {
                    // Add the node to the list if it is actionable
                    if (node.isClickable || node.isFocusable) {
                        allNodes.add(node)
                    }

                    for (i in 0 until node.childCount) {
                        val child = node.getChild(i)
                        if (child != null) {
                            q.add(child)
                        }
                    }
                }
            }

            return@withContext allNodes
        }

    /**
     * Group nodes into rows
     * @param nodes The nodes to group
     * @return The list of rows
     */
    private fun groupNodesIntoRows(nodes: List<AccessibilityNodeInfo>): List<List<AccessibilityNodeInfo>> {
        val sortedNodes = nodes.sortedBy { node ->
            val rect = Rect()
            node.getBoundsInScreen(rect)
            rect.top
        }

        val rows: MutableList<List<AccessibilityNodeInfo>> = mutableListOf()
        var currentRow: MutableList<AccessibilityNodeInfo> = mutableListOf()

        sortedNodes.forEach { node ->
            val nodeRect = Rect()
            node.getBoundsInScreen(nodeRect)

            if (currentRow.isEmpty()) {
                currentRow.add(node)
            } else {
                val lastNodeInRow = currentRow.last()
                val lastNodeRect = Rect()
                lastNodeInRow.getBoundsInScreen(lastNodeRect)

                if (abs(nodeRect.top - lastNodeRect.top) <= ROW_TOLERANCE ||
                    abs(nodeRect.bottom - lastNodeRect.bottom) <= ROW_TOLERANCE
                ) {
                    currentRow.add(node)
                } else {
                    rows.add(ArrayList(currentRow))
                    currentRow = mutableListOf(node)
                }
            }
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        return rows
    }
}