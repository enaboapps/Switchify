package com.enaboapps.switchify.service.nodes

import android.content.Context
import com.enaboapps.switchify.service.scanning.ScanReceiver
import com.enaboapps.switchify.service.scanning.ScanTree
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This class is responsible for scanning the nodes
 */
class NodeScanner(context: Context) : NodeUpdateDelegate {
    private val job = Job()
    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + job) // Use Main dispatcher for UI operations.

    val scanTree = ScanTree(context, true)

    private var nodes: List<Node> = emptyList()

    init {
        NodeExaminer.nodeUpdateDelegate = this
    }

    override fun onNodesUpdated(nodes: List<Node>) {
        this.nodes = nodes

        scanTree.buildTree(nodes)

        if (nodes.isEmpty()) {
            coroutineScope.launch {
                delay(5000) // Non-blocking delay for 5 seconds
                if (ScanReceiver.state == ScanReceiver.ReceiverState.ITEM_SCAN && this@NodeScanner.nodes.isEmpty()) {
                    scanTree.reset()
                    ScanReceiver.state = ScanReceiver.ReceiverState.CURSOR
                }
            }
        } else {
            job.cancelChildren() // Cancel any ongoing coroutine tasks if nodes are found
        }
    }

    fun clear() {
        job.cancel() // Cancel all coroutines when the scanner is no longer needed.
    }
}