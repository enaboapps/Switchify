package com.enaboapps.switchify.service.methods.nodes

import android.content.Context
import android.graphics.PointF
import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.service.utils.ScreenUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import java.util.Queue
import kotlin.math.sqrt

/**
 * NodeExaminer is responsible for examining accessibility nodes within an application's UI.
 * It provides methods to find, filter, and analyze nodes, as well as to observe changes in the node structure.
 */
object NodeExaminer {
    /** Holds the list of all nodes. */
    private var allNodes: List<Node> = emptyList()

    /** Holds the list of actionable nodes. */
    private var actionableNodes: List<Node> = emptyList()

    /** SharedFlow for emitting updates to the list of actionable nodes. */
    private val updateFlow = MutableSharedFlow<List<Node>>(replay = 1, extraBufferCapacity = 1)

    /**
     * Provides a Flow to observe changes in the list of actionable nodes.
     *
     * @return A Flow emitting lists of Node objects whenever there's an update.
     */
    fun observeNodes(): Flow<List<Node>> = updateFlow.asSharedFlow()

    /**
     * Initiates the process of finding and updating the list of nodes.
     * It flattens the accessibility tree starting from the rootNode, filters out nodes not on the screen,
     * and emits an update if the actionable nodes have changed.
     *
     * @param rootNode The root node to start the examination from.
     * @param context The current context, used to get screen dimensions for filtering nodes.
     * @param coroutineScope The CoroutineScope in which to perform the node examination.
     */
    suspend fun findNodes(
        rootNode: AccessibilityNodeInfo,
        context: Context,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch(Dispatchers.Default) {
            val newNodeInfos = flattenTree(rootNode)
            allNodes = newNodeInfos.map { Node.fromAccessibilityNodeInfo(it) }

            val newActionableNodes = newNodeInfos
                .filter { it.isClickable || it.isLongClickable }
                .map { Node.fromAccessibilityNodeInfo(it) }

            val width = ScreenUtils.getWidth(context)
            val height = ScreenUtils.getHeight(context)

            val filteredNewActionableNodes = newActionableNodes.filter {
                it.getLeft() >= 0 && it.getTop() >= 0 &&
                        it.getLeft() <= width && it.getTop() <= height &&
                        it.getWidth() > 0 && it.getHeight() > 0
            }

            if (actionableNodes != filteredNewActionableNodes) {
                actionableNodes = filteredNewActionableNodes
                updateFlow.emit(actionableNodes)
            }
        }
    }

    /**
     * Flattens the given tree of AccessibilityNodeInfo objects into a list.
     * This method explores the tree breadth-first to collect all nodes.
     *
     * @param rootNode The root node of the tree to start flattening from.
     * @return A list of all AccessibilityNodeInfo objects in the tree.
     */
    private suspend fun flattenTree(rootNode: AccessibilityNodeInfo): List<AccessibilityNodeInfo> =
        withContext(Dispatchers.Default) {
            val nodes: MutableList<AccessibilityNodeInfo> = ArrayList()
            val q: Queue<AccessibilityNodeInfo> = LinkedList()
            q.add(rootNode)

            while (q.isNotEmpty()) {
                val node = q.poll()
                node?.let { accessibilityNodeInfo ->
                    nodes.add(accessibilityNodeInfo)
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
     * @return The node that can perform the given action at the given point, or null if no such node exists.
     */
    fun findNodeForAction(point: PointF, actionType: Node.ActionType): Node? {
        return allNodes.find { it.containsPoint(point) && it.isActionable(actionType) }
    }

    /**
     * Checks if a node can perform any edit actions at the given point.
     *
     * @param point The point to check for edit actions.
     * @return True if a node can perform any edit actions at the given point, false otherwise.
     */
    fun canPerformEditActions(point: PointF): Boolean {
        return findNodeForAction(point, Node.ActionType.CUT) != null ||
                findNodeForAction(point, Node.ActionType.COPY) != null ||
                findNodeForAction(point, Node.ActionType.PASTE) != null
    }

    /**
     * Finds the closest node to a given point on the screen.
     *
     * @param point The point for which to find the closest node.
     * @return The closest node's center point. Returns the original point if no close node is found.
     */
    fun getClosestNodeToPoint(point: PointF): PointF {
        val maxDistance = 200f
        return actionableNodes
            .map { PointF(it.getMidX().toFloat(), it.getMidY().toFloat()) }
            .minByOrNull { distanceBetweenPoints(point, it) }
            ?.takeIf { distanceBetweenPoints(point, it) < maxDistance }
            ?: point
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
        return sqrt((xDiff * xDiff + yDiff * yDiff))
    }
}