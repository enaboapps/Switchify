package com.enaboapps.switchify.service.techniques.nodes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal interface AccessibilityActionMenuActions {
    suspend fun open(target: NodeActionTarget, isCurrent: () -> Boolean)
    suspend fun replace(target: NodeActionTarget, isCurrent: () -> Boolean)
    suspend fun showMainWithoutActions(isCurrent: () -> Boolean)
    suspend fun closeAll(isCurrent: () -> Boolean)
}

internal class AccessibilityActionCoordinator(
    private val scope: CoroutineScope,
    private val resolver: NodeActionResolver,
    private val menuActions: AccessibilityActionMenuActions
) {
    private val stateLock = Any()
    private var generation = 0L
    private var resolving = false

    fun open(locator: NodeActionLocator) {
        val operationGeneration = beginOperation() ?: return
        scope.launch {
            try {
                val resolution = resolveSafely(locator, emptySet())
                if (!isCurrent(operationGeneration)) return@launch
                when (resolution) {
                    NodeActionResolution.SourceMissing -> close(operationGeneration)
                    NodeActionResolution.TargetMissing -> showMain(operationGeneration)
                    is NodeActionResolution.Resolved -> {
                        val target = NodeActionTarget(locator, resolution.target.actions)
                        if (target.actions.isEmpty()) {
                            showMain(operationGeneration)
                        } else {
                            menuActions.open(target) { isCurrent(operationGeneration) }
                        }
                    }
                }
            } finally {
                finishOperation(operationGeneration)
            }
        }
    }

    fun select(target: NodeActionTarget, actionId: Int) {
        val operationGeneration = beginOperation() ?: return
        scope.launch {
            try {
                val resolution = resolveSafely(target.locator, target.excludedActionIds)
                if (!isCurrent(operationGeneration)) return@launch
                when (resolution) {
                    NodeActionResolution.SourceMissing -> close(operationGeneration)
                    NodeActionResolution.TargetMissing -> showMain(operationGeneration)
                    is NodeActionResolution.Resolved -> handleResolvedSelection(
                        target,
                        actionId,
                        resolution.target,
                        operationGeneration
                    )
                }
            } finally {
                finishOperation(operationGeneration)
            }
        }
    }

    fun cancel() {
        synchronized(stateLock) {
            generation += 1
            resolving = false
        }
    }

    internal fun isResolving(): Boolean = synchronized(stateLock) { resolving }

    private suspend fun handleResolvedSelection(
        target: NodeActionTarget,
        actionId: Int,
        resolvedTarget: ResolvedNodeActionTarget,
        operationGeneration: Long
    ) {
        if (resolvedTarget.actions.none { it.id == actionId }) {
            refreshOrShowMain(
                target.copy(actions = resolvedTarget.actions),
                operationGeneration
            )
            return
        }
        if (resolvedTarget.perform(actionId)) {
            close(operationGeneration)
            return
        }
        val excludedActionIds = target.excludedActionIds + actionId
        refreshOrShowMain(
            NodeActionTarget(
                locator = target.locator,
                actions = resolvedTarget.actions.filterNot { it.id == actionId },
                excludedActionIds = excludedActionIds
            ),
            operationGeneration
        )
    }

    private suspend fun refreshOrShowMain(
        target: NodeActionTarget,
        operationGeneration: Long
    ) {
        if (target.actions.isEmpty()) {
            showMain(operationGeneration)
        } else {
            menuActions.replace(target) { isCurrent(operationGeneration) }
        }
    }

    private suspend fun showMain(operationGeneration: Long) {
        menuActions.showMainWithoutActions { isCurrent(operationGeneration) }
    }

    private suspend fun close(operationGeneration: Long) {
        menuActions.closeAll { isCurrent(operationGeneration) }
    }

    private fun beginOperation(): Long? = synchronized(stateLock) {
        if (resolving) return null
        generation += 1
        resolving = true
        generation
    }

    private fun finishOperation(operationGeneration: Long) {
        synchronized(stateLock) {
            if (generation == operationGeneration) resolving = false
        }
    }

    private fun isCurrent(operationGeneration: Long): Boolean = synchronized(stateLock) {
        generation == operationGeneration && resolving
    }

    private fun resolveSafely(
        locator: NodeActionLocator,
        excludedActionIds: Set<Int>
    ): NodeActionResolution = runCatching {
        resolver.resolve(locator, excludedActionIds)
    }.getOrElse {
        NodeActionResolution.SourceMissing
    }
}
