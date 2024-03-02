package com.enaboapps.switchify.service.nodes

import android.content.Context
import com.enaboapps.switchify.service.scanning.ScanTree

/**
 * This class is responsible for scanning the nodes
 */
class NodeScanner(context: Context) : NodeUpdateDelegate {
    val scanTree = ScanTree(context)

    init {
        NodeExaminer.nodeUpdateDelegate = this
    }

    override fun onNodesUpdated(nodes: List<Node>) {
        scanTree.buildTree(nodes)
    }
}