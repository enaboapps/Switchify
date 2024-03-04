package com.enaboapps.switchify.service.nodes

import android.graphics.PointF
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import kotlin.math.sqrt

/**
 * This interface delegates the node updates
 */
interface NodeUpdateDelegate {
    fun onNodesUpdated(nodes: List<Node>)
}

/**
 * This class is responsible for examining the nodes
 */
object NodeExaminer {

    var nodeUpdateDelegate: NodeUpdateDelegate? = null

    private var currentNodes: List<Node> = emptyList()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var examineJob: Job? = null

    /**
     * Find the nodes in the tree
     * @param rootNode The root node to start from
     */
    fun findNodes(rootNode: AccessibilityNodeInfo) {
        examineJob?.cancel()
        examineJob = coroutineScope.launch {
            val allNodes = flattenTree(rootNode)
            currentNodes = allNodes.map { Node.fromAccessibilityNodeInfo(it) }
            nodeUpdateDelegate?.onNodesUpdated(currentNodes)
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
                    if (node.isClickable) {
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

        for (node in currentNodes) {
            val nodeCenter = PointF(node.getCenterX().toFloat(), node.getCenterY().toFloat())
            val distance = distanceBetweenPoints(point, nodeCenter)
            // The distance has to be less than the max distance and less than the current closest distance
            if (distance < max && distance < closestDistance) {
                closestNodePoint = nodeCenter
                closestDistance = distance
                wasFound = true
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
}