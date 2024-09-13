package com.enaboapps.switchify.service.nodes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.keyboard.KeyboardAccessibilityManager
import com.enaboapps.switchify.keyboard.KeyboardLayoutInfo
import com.enaboapps.switchify.keyboard.SwitchifyKeyboardService
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import com.enaboapps.switchify.service.selection.AutoSelectionHandler
import com.enaboapps.switchify.service.utils.ScreenWatcher
import com.google.gson.Gson
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
 */
class NodeScanner : NodeUpdateDelegate {
    /**
     * Context in which the NodeScanner is started.
     */
    private lateinit var context: Context

    /**
     * ScanTree instance used for managing the scanning process.
     */
    lateinit var scanTree: ScanTree

    /**
     * List of Node instances that are currently being managed on the screen.
     */
    private var screenNodes: List<Node> = emptyList()

    /**
     * List of Node instances that are currently being managed on the keyboard.
     */
    private var keyboardNodes: List<Node> = emptyList()

    /**
     * BroadcastReceiver that listens for keyboard layout updates.
     */
    private val keyboardLayoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val jsonLayoutInfo =
                intent.getStringExtra(KeyboardAccessibilityManager.EXTRA_KEYBOARD_LAYOUT_INFO)
            val layoutInfo = Gson().fromJson(jsonLayoutInfo, KeyboardLayoutInfo::class.java)
            updateNodesWithLayoutInfo(layoutInfo)
        }
    }

    /**
     * BroadcastReceiver that listens for keyboard show events.
     */
    private val keyboardShowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isKeyboardVisible = true
            AutoSelectionHandler.setBypassAutoSelect(true)
            println("Keyboard shown")
        }
    }

    /**
     * BroadcastReceiver that listens for keyboard hide events.
     */
    private val keyboardHideReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            escapeKeyboardScan()
        }
    }

    /**
     * Boolean flag indicating whether the keyboard is visible.
     */
    private var isKeyboardVisible = false

    /**
     * Screen watcher instance used for watching the screen.
     */
    private val screenWatcher = ScreenWatcher(onScreenSleep = {
        escapeKeyboardScan()
    })

    /**
     * Starts the NodeScanner.
     * Sets the nodeUpdateDelegate of the NodeExaminer to this instance and starts the timeout.
     * Also initializes the scanTree with the context.
     *
     * @param context The context in which the NodeScanner is started.
     */
    fun start(context: Context) {
        this.context = context
        NodeExaminer.nodeUpdateDelegate = this
        startTimeoutToRevertToCursor()
        scanTree = ScanTree(
            context = context,
            stopScanningOnSelect = true
        )
        screenWatcher.register(context)
    }

    /**
     * Registers the required event receivers.
     *
     * @param context The context in which the receivers are registered.
     */
    fun registerEventReceivers(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            keyboardLayoutReceiver,
            IntentFilter(KeyboardAccessibilityManager.ACTION_KEYBOARD_LAYOUT_INFO)
        )
        LocalBroadcastManager.getInstance(context).registerReceiver(
            keyboardShowReceiver,
            IntentFilter(SwitchifyKeyboardService.ACTION_KEYBOARD_SHOW)
        )
        LocalBroadcastManager.getInstance(context).registerReceiver(
            keyboardHideReceiver,
            IntentFilter(SwitchifyKeyboardService.ACTION_KEYBOARD_HIDE)
        )
    }

    /**
     * Escapes the keyboard scan.
     */
    private fun escapeKeyboardScan() {
        isKeyboardVisible = false
        setScreenNodes(screenNodes)
        setKeyboardNodes(emptyList())
        AutoSelectionHandler.setBypassAutoSelect(false)
        println("Keyboard hidden, updating nodes")
    }

    /**
     * Updates the nodes with the layout info from the keyboard.
     * It creates a new list of nodes from the layout info and updates the nodes.
     *
     * @param layoutInfo KeyboardLayoutInfo instance.
     */
    private fun updateNodesWithLayoutInfo(layoutInfo: KeyboardLayoutInfo) {
        val newNodes = layoutInfo.keys.map { Node.fromKeyInfo(it) }
        newNodes.forEach { println(it) }
        setKeyboardNodes(newNodes)
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
            if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN && this@NodeScanner.screenNodes.isEmpty() && this@NodeScanner.keyboardNodes.isEmpty()) {
                withContext(Dispatchers.Main) {
                    scanTree.reset()
                    ScanMethod.setType(ScanMethod.MethodType.CURSOR)
                    println("ScanMethod changed to cursor")
                }
            } else {
                println("ScanMethod not changed, nodes.size: ${this@NodeScanner.screenNodes.size}")
            }
        }
    }

    /**
     * Sets the keyboard nodes.
     *
     * @param nodes List of new Node instances.
     */
    private fun setKeyboardNodes(nodes: List<Node>) {
        this.keyboardNodes = nodes
        if (isKeyboardVisible) {
            updateNodes(nodes)
        }
    }

    /**
     * Sets the screen nodes.
     *
     * @param nodes List of new Node instances.
     */
    private fun setScreenNodes(nodes: List<Node>) {
        this.screenNodes = nodes
        if (!isKeyboardVisible) {
            updateNodes(nodes)
        }
    }

    /**
     * Updates the nodes and rebuilds the scanTree.
     * If no nodes are present, it starts the timeout.
     *
     * @param nodes List of new Node instances.
     */
    private fun updateNodes(nodes: List<Node>) {
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

    /**
     * Called when the nodes are updated.
     * Sets the screen nodes.
     *
     * @param nodes List of new Node instances.
     */
    override fun onNodesUpdated(nodes: List<Node>) {
        setScreenNodes(nodes)
    }
}