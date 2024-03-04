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
import kotlin.coroutines.CoroutineContext

/**
 * This class is responsible for scanning the nodes
 */
class NodeScanner(context: Context) : NodeUpdateDelegate, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job() // Ensures operations are performed on the main thread.

    val scanTree = ScanTree(context, true)

    init {
        NodeExaminer.nodeUpdateDelegate = this
    }

    override fun onNodesUpdated(nodes: List<Node>) {
        scanTree.buildTree(nodes)

        if (nodes.isEmpty()) {
            launch {
                delay(5000) // Non-blocking delay for 5 seconds
                if (ScanReceiver.state == ScanReceiver.ReceiverState.ITEM_SCAN) {
                    scanTree.reset()
                    ScanReceiver.state = ScanReceiver.ReceiverState.CURSOR
                }
            }
        } else {
            coroutineContext.cancelChildren() // Cancel any ongoing coroutine tasks if nodes are found
        }
    }
}