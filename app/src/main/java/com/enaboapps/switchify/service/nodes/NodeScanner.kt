package com.enaboapps.switchify.service.nodes

import android.content.Context
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NodeScanner is a class that handles the scanning of nodes.
 * It implements the NodeUpdateDelegate interface.
 * It uses a ScanTree instance to manage the scanning process.
 *
 * @property scanTree ScanTree instance used for managing the scanning process.
 * @property nodes List of Node instances that are currently being managed.
 */
class NodeScanner private constructor(context: Context) : NodeUpdateDelegate {
    val scanTree =
        ScanTree(
            context,
            stopScanningOnSelect = true,
            individualHighlightingItemsInTreeItem = false
        )

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
     * Sets up the scanTree.
     */
    fun setup() {
        scanTree.setup()
    }

    /**
     * Starts a timeout that resets the scanTree and changes the state of the ScanMethod
     * if the state is ITEM_SCAN and there are no nodes after 5 seconds.
     */
    fun startTimeoutToRevertToCursor() {
        val timeoutJob = Job()
        val timeoutScope = CoroutineScope(Dispatchers.Default + timeoutJob)

        timeoutScope.launch {
            delay(5000)
            if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN && this@NodeScanner.nodes.isEmpty()) {
                withContext(Dispatchers.Main) {
                    scanTree.reset()
                    ScanMethod.setType(ScanMethod.MethodType.CURSOR)
                    println("ScanMethod changed to cursor")
                }
            } else {
                println("ScanMethod not changed, nodes.size: ${this@NodeScanner.nodes.size}")
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

        if (nodes.isEmpty()) {
            startTimeoutToRevertToCursor()
        }
    }

    /**
     * Cleans up the NodeScanner by shutting down the scanTree.
     */
    fun cleanup() {
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