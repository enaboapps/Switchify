package com.enaboapps.switchify.service.nodes

import android.graphics.PointF
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
import kotlin.math.sqrt

object NodeExaminer {

    var currentRows: List<List<Node>> = emptyList()

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
     * Get the closest node to the given point
     * @param point The point to search for
     * @return The closest node point
     */
    fun getClosestNodeToPoint(point: PointF): PointF {
        var closestNodePoint = PointF(Float.MAX_VALUE, Float.MAX_VALUE)
        var closestDistance = Float.MAX_VALUE

        val max = 200

        var wasFound = false

        for (row in currentRows) {
            for (node in row) {
                val nodeCenter = PointF(node.getCenterX().toFloat(), node.getCenterY().toFloat())
                val distance = distanceBetweenPoints(point, nodeCenter)
                // The distance has to be less than the max distance and less than the current closest distance
                if (distance < max && distance < closestDistance) {
                    closestNodePoint = nodeCenter
                    closestDistance = distance
                    wasFound = true
                }
            }
        }

        return if (wasFound) {
            closestNodePoint
        } else {
            point
        }
    }

    /**
     * Get the distance between two points
     * @param point1 The first point
     * @param point2 The second point
     * @return The distance between the two points
     */
    private fun distanceBetweenPoints(point1: PointF, point2: PointF): Float {
        val xDiff = point1.x - point2.x
        val yDiff = point1.y - point2.y
        return sqrt((xDiff * xDiff + yDiff * yDiff).toDouble()).toFloat()
    }

    /**
     * Group nodes into rows
     * @param nodes The nodes to group
     * @return The list of rows
     */
    private fun groupNodesIntoRows(nodes: List<AccessibilityNodeInfo>): List<List<Node>> {
        val sortedNodes = nodes.sortedBy { node ->
            val rect = Rect()
            node.getBoundsInScreen(rect)
            rect.top
        }

        val rows: MutableList<List<Node>> = mutableListOf()
        var currentRow: MutableList<Node> = mutableListOf()

        sortedNodes.forEach { node ->
            val nodeRect = Rect()
            node.getBoundsInScreen(nodeRect)

            if (currentRow.isEmpty()) {
                currentRow.add(Node.fromAccessibilityNodeInfo(node))
            } else {
                val lastNodeInRow = currentRow.last()
                if (abs(nodeRect.top - lastNodeInRow.getY()) < ROW_TOLERANCE) {
                    currentRow.add(Node.fromAccessibilityNodeInfo(node))
                } else {
                    rows.add(ArrayList(currentRow))
                    currentRow = mutableListOf(Node.fromAccessibilityNodeInfo(node))
                }
            }
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        return rows
    }
}