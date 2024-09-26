package com.enaboapps.switchify.service.methods.nodes

import android.content.Context
import android.graphics.PointF
import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import kotlin.math.sqrt

/**
 * This interface is used to delegate node updates.
 */
interface NodeUpdateDelegate {
    fun onNodesUpdated(nodes: List<Node>)
}

/**
 * This object is responsible for examining accessibility nodes within an application's UI.
 */
object NodeExaminer {

    // Delegate to notify about node updates.
    var nodeUpdateDelegate: NodeUpdateDelegate? = null

    // Holds the list of all nodes.
    private var allNodes: List<Node> = emptyList()

    // Holds the list of actionable nodes.
    private var actionableNodes: List<Node> = emptyList()

    // Scope for launching coroutines.
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // Job for managing the examination process, allowing it to be cancelled if a new one starts.
    private var examineJob: Job? = null

    /**
     * Initiates the process of finding and updating the list of nodes.
     * It first flattens the accessibility tree starting from the rootNode, then filters
     * out nodes not on the screen, and finally updates the currentNodes if they differ.
     *
     * @param rootNode The root node to start the examination from.
     * @param context The current context, used to get screen dimensions for filtering nodes.
     */
    fun findNodes(rootNode: AccessibilityNodeInfo, context: Context) {
        // Cancel any ongoing examination job.
        examineJob?.cancel()
        examineJob = coroutineScope.launch {
            // Flatten the accessibility tree to get all nodes.
            val newNodeInfos = flattenTree(rootNode)

            // Store all nodes for future reference.
            allNodes = newNodeInfos.map { Node.fromAccessibilityNodeInfo(it) }

            // Create a list of actionable nodes from the new nodes.
            val newActionableNodes = newNodeInfos.filter { it.isClickable || it.isLongClickable }
                .map { Node.fromAccessibilityNodeInfo(it) }

            // Get screen dimensions.
            val width = ScreenUtils.getWidth(context)
            val height = ScreenUtils.getHeight(context)

            // Filter nodes to those that are on-screen and have non-zero width and height.
            val filteredNewActionableNodes =
                newActionableNodes.filter {
                    it.getLeft() >= 0 && it.getTop() >= 0 &&
                            it.getLeft() <= width && it.getTop() <= height &&
                            it.getWidth() > 0 && it.getHeight() > 0
                }

            // Compare the current nodes with the new ones using sets.
            if (actionableNodes.toSet() != filteredNewActionableNodes.toSet()) {
                actionableNodes = filteredNewActionableNodes
                // Notify the delegate if there's an update.
                nodeUpdateDelegate?.onNodesUpdated(actionableNodes)
            }
        }
    }

    /**
     * Flattens the given tree of AccessibilityNodeInfo objects into a list.
     * This method explores the tree breadth-first to collect all nodes.
     *
     * @param rootNode The root node of the tree to start flattening from.
     * @return A list of all nodes in the tree.
     */
    private suspend fun flattenTree(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> =
        withContext(Dispatchers.IO) {
            val nodes: MutableList<AccessibilityNodeInfo> = ArrayList()
            val q: Queue<AccessibilityNodeInfo> = LinkedList()
            q.add(rootNode)

            while (q.isNotEmpty()) {
                val node = q.poll()
                node?.let { accessibilityNodeInfo ->
                    nodes.add(accessibilityNodeInfo)
                    // Add all child nodes to the queue for further examination.
                    for (i in 0 until node.childCount) {
                        node.getChild(i)?.let { q.add(it) }
                    }
                }
            }
            nodes
        }

    /**
     * Finds the node that can perform the given action at the given point.
     *
     * @param point The point to find the node at.
     * @param actionType The action to find the node for.
     * @return The node that can perform the given action at the given point.
     */
    fun findNodeForAction(point: PointF, actionType: Node.ActionType): Node? {
        allNodes.forEach { node ->
            if (node.containsPoint(point) && node.isActionable(actionType)) {
                return node
            }
        }
        return null
    }

    /**
     * Checks if a node can perform any edit actions at the given point.
     *
     * @param point The point to check for edit actions.
     * @return True if a node can perform any edit actions at the given point, false otherwise.
     */
    fun canPerformEditActions(point: PointF): Boolean {
        val cutNode = findNodeForAction(point, Node.ActionType.CUT)
        val copyNode = findNodeForAction(point, Node.ActionType.COPY)
        val pasteNode = findNodeForAction(point, Node.ActionType.PASTE)
        return cutNode != null || copyNode != null || pasteNode != null
    }

    /**
     * Finds the closest node to a given point on the screen.
     *
     * @param point The point for which to find the closest node.
     * @return The closest node's center point. Returns the original point if no close node is found.
     */
    fun getClosestNodeToPoint(point: PointF): PointF {
        var closestNodePoint = PointF(Float.MAX_VALUE, Float.MAX_VALUE)
        var closestDistance = Float.MAX_VALUE

        val maxDistance = 200

        for (node in actionableNodes) {
            val nodeCenter = PointF(node.getMidX().toFloat(), node.getMidY().toFloat())
            val distance = distanceBetweenPoints(point, nodeCenter)
            if (distance < maxDistance && distance < closestDistance) {
                closestNodePoint = nodeCenter
                closestDistance = distance
            }
        }

        return if (closestDistance < Float.MAX_VALUE) closestNodePoint else point
    }

    /**
     * Calculates the distance between two points.
     *
     * @param point1 The first point.
     * @param point2 The second point.
     * @return The distance between point1 and point2.
     */
    private fun distanceBetweenPoints(point1: PointF, point2: PointF): Float {
        val xDiff = point1.x - point2.x
        val yDiff = point1.y - point2.y
        return sqrt((xDiff * xDiff + yDiff * yDiff).toDouble()).toFloat()
    }
}