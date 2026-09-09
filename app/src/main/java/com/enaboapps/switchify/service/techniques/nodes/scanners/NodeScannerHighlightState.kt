package com.enaboapps.switchify.service.techniques.nodes.scanners

import com.enaboapps.switchify.service.window.overlay.OverlayTarget

internal enum class NodeScannerHighlightRole {
    ITEM,
    ROW,
    ESCAPE
}

internal data class NodeScannerHighlightSpec(
    val role: NodeScannerHighlightRole,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val target: OverlayTarget,
    val owner: String? = null,
    val screenBounds: ScanHighlightBounds? = null,
    val intervalAfterSequence: Long = 0L
) {
    fun usesSpotlight(requested: Boolean, unavailableTarget: OverlayTarget?): Boolean =
        owner != null && requested && screenBounds?.isUsable == true && unavailableTarget != target

    fun animatesFrom(previous: NodeScannerHighlightSpec, movementEnabled: Boolean,
        systemAnimationsEnabled: Boolean): Boolean = owner != null && previous.owner == owner &&
        movementEnabled && systemAnimationsEnabled &&
        NodeScannerHighlightTransitions.sameCoordinateSpace(previous.target, target)
}

internal data class NodeScannerHighlightState(
    val role: NodeScannerHighlightRole,
    val target: OverlayTarget
)

internal enum class NodeScannerHighlightTransition {
    ATTACH,
    UPDATE,
    REPLACE_TARGET,
    REMOVE,
    IGNORE
}

internal object NodeScannerHighlightTransitions {
    fun show(
        current: NodeScannerHighlightState?,
        next: NodeScannerHighlightSpec
    ): NodeScannerHighlightTransition {
        return when {
            current == null -> NodeScannerHighlightTransition.ATTACH
            sameCoordinateSpace(current.target, next.target) -> NodeScannerHighlightTransition.UPDATE
            else -> NodeScannerHighlightTransition.REPLACE_TARGET
        }
    }

    fun sameCoordinateSpace(first: OverlayTarget, second: OverlayTarget): Boolean = when {
        first is OverlayTarget.Display && second is OverlayTarget.Display -> first == second
        first is OverlayTarget.Window && second is OverlayTarget.Window ->
            first.displayId == second.displayId && first.accessibilityWindowId == second.accessibilityWindowId &&
                first.windowType == second.windowType
        else -> false
    }

    fun hide(
        current: NodeScannerHighlightState?,
        roles: Set<NodeScannerHighlightRole>
    ): NodeScannerHighlightTransition {
        return if (current?.role in roles) {
            NodeScannerHighlightTransition.REMOVE
        } else {
            NodeScannerHighlightTransition.IGNORE
        }
    }

    fun isCurrentEpoch(commandEpoch: Long, rendererEpoch: Long): Boolean {
        return commandEpoch == rendererEpoch
    }
}

internal class NodeScannerVisualBatch(val owner: String, var epoch: Long, private val intervalAfterSequence: Long = 0L) {
    var spec: NodeScannerHighlightSpec? = null
        private set
    val hideRoles = mutableSetOf<NodeScannerHighlightRole>()

    fun show(next: NodeScannerHighlightSpec) { spec = next.copy(intervalAfterSequence = intervalAfterSequence) }

    fun hide(roles: Set<NodeScannerHighlightRole>) {
        hideRoles.addAll(roles)
        if (spec?.role in roles) spec = null
    }

    fun reset(nextEpoch: Long) {
        spec = null
        hideRoles.clear()
        epoch = nextEpoch
    }
}
