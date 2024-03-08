package com.enaboapps.switchify.service.nodes

import android.content.Context
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * NodeScanner is a class that handles the scanning of nodes.
 * It implements the NodeUpdateDelegate interface.
 *
 * @property job Job instance used for managing coroutines.
 * @property coroutineScope CoroutineScope instance used for launching coroutines.
 * @property scanTree ScanTree instance used for managing the scanning process.
 * @property nodes List of Node instances that are currently being managed.
 */
class NodeScanner private constructor(context: Context) : NodeUpdateDelegate {
    private val job = Job()
    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + job)

    val scanTree =
        ScanTree(context, stopScanningOnSelect = true, individualHighlightingItemsInRow = false)

    private var nodes: List<Node> = emptyList()

    /**
     * Initialization block for the NodeScanner class.
     * Sets the nodeUpdateDelegate of the NodeExaminer to this instance and starts the timeout.
     */
    init {
        NodeExaminer.nodeUpdateDelegate = this
        startTimeoutToRevertToCursor()
    }

    /**
     * Starts a timeout that resets the scanTree and changes the state of the ScanMethod
     * if the state is ITEM_SCAN and there are no nodes after 5 seconds.
     */
    fun startTimeoutToRevertToCursor() {
        coroutineScope.launch {
            delay(5000)
            if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN && this@NodeScanner.nodes.isEmpty()) {
                scanTree.reset()
                ScanMethod.setType(ScanMethod.MethodType.CURSOR)
            }
        }
    }

    /**
     * Updates the nodes and rebuilds the scanTree when new nodes are detected.
     * If nodes are present, it cancels any children of the job.
     * If no nodes are present, it starts the timeout.
     *
     * @param nodes List of new Node instances.
     */
    override fun onNodesUpdated(nodes: List<Node>) {
        this.nodes = nodes

        scanTree.buildTree(nodes)

        if (nodes.isNotEmpty()) {
            job.cancelChildren()
        } else {
            startTimeoutToRevertToCursor()
        }
    }

    /**
     * Cleans up the NodeScanner by cancelling the job and shutting down the scanTree.
     */
    fun cleanup() {
        job.cancel()
        scanTree.shutdown()
    }

    companion object {
        @Volatile
        private var INSTANCE: NodeScanner? = null

        fun getInstance(context: Context): NodeScanner {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NodeScanner(context).also { INSTANCE = it }
            }
        }
    }
}