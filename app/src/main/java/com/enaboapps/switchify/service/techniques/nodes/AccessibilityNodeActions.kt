package com.enaboapps.switchify.service.techniques.nodes

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.R

internal data class ReportedNodeAction(
    val id: Int,
    val label: String?
)

internal data class NodeActionDescriptor(
    val id: Int,
    val label: String
)

internal class NodeActionTarget(
    val actions: List<NodeActionDescriptor>,
    private val actionPerformer: (Int) -> Boolean
) {
    fun perform(actionId: Int): Boolean = actionPerformer(actionId)
}

internal data class NodeActionCandidate<T>(
    val target: T,
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
    val traversalOrder: Int = 0
) {
    fun contains(x: Float, y: Float): Boolean =
        x >= left && x <= left + width && y >= top && y <= top + height
}

internal object NodeActionTargetSelector {
    fun <T> selectSmallest(
        x: Float,
        y: Float,
        candidates: List<NodeActionCandidate<T>>
    ): T? = candidates
        .asSequence()
        .filter { it.width > 0 && it.height > 0 && it.contains(x, y) }
        .minWithOrNull(
            compareBy<NodeActionCandidate<T>> { it.width.toLong() * it.height.toLong() }
                .thenByDescending { it.traversalOrder }
        )
        ?.target
}

internal object NodeActionPolicy {
    fun resolve(
        actions: List<ReportedNodeAction>,
        standardLabel: (Int) -> String?,
        isExcluded: (Int) -> Boolean
    ): List<NodeActionDescriptor> {
        val seen = mutableSetOf<Int>()
        return actions.mapNotNull { action ->
            if (!seen.add(action.id) || isExcluded(action.id)) {
                return@mapNotNull null
            }

            val defaultLabel = standardLabel(action.id)
            val label = action.label?.trim()?.takeIf { it.isNotEmpty() } ?: defaultLabel
            if (label == null) return@mapNotNull null

            NodeActionDescriptor(action.id, label)
        }
    }

}

internal object AndroidNodeActionLabels {
    fun standardLabel(context: Context, actionId: Int): String? =
        standardLabelResource(actionId)?.let(context::getString)

    fun isExcluded(actionId: Int): Boolean = actionId in excludedStandardActionIds

    private fun standardLabelResource(actionId: Int): Int? = when (actionId) {
        AccessibilityNodeInfo.ACTION_CLICK -> R.string.accessibility_action_click
        AccessibilityNodeInfo.ACTION_LONG_CLICK -> R.string.accessibility_action_long_click
        AccessibilityNodeInfo.AccessibilityAction.ACTION_CONTEXT_CLICK.id -> R.string.accessibility_action_context_click
        AccessibilityNodeInfo.ACTION_SELECT -> R.string.accessibility_action_select
        AccessibilityNodeInfo.ACTION_CLEAR_SELECTION -> R.string.accessibility_action_clear_selection
        AccessibilityNodeInfo.ACTION_EXPAND -> R.string.accessibility_action_expand
        AccessibilityNodeInfo.ACTION_COLLAPSE -> R.string.accessibility_action_collapse
        AccessibilityNodeInfo.ACTION_DISMISS -> R.string.accessibility_action_dismiss
        AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> R.string.accessibility_action_scroll_forward
        AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> R.string.accessibility_action_scroll_backward
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id -> R.string.accessibility_action_scroll_up
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id -> R.string.accessibility_action_scroll_down
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id -> R.string.accessibility_action_scroll_left
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id -> R.string.accessibility_action_scroll_right
        AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_UP.id -> R.string.accessibility_action_page_up
        AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_DOWN.id -> R.string.accessibility_action_page_down
        AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_LEFT.id -> R.string.accessibility_action_page_left
        AccessibilityNodeInfo.AccessibilityAction.ACTION_PAGE_RIGHT.id -> R.string.accessibility_action_page_right
        AccessibilityNodeInfo.ACTION_COPY -> R.string.accessibility_action_copy
        AccessibilityNodeInfo.ACTION_CUT -> R.string.accessibility_action_cut
        AccessibilityNodeInfo.ACTION_PASTE -> R.string.accessibility_action_paste
        AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id -> R.string.accessibility_action_ime_enter
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_TOOLTIP.id -> R.string.accessibility_action_show_tooltip
        AccessibilityNodeInfo.AccessibilityAction.ACTION_HIDE_TOOLTIP.id -> R.string.accessibility_action_hide_tooltip
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id -> R.string.accessibility_action_show_on_screen
        else -> null
    }

    private val excludedStandardActionIds = setOf(
        AccessibilityNodeInfo.ACTION_FOCUS,
        AccessibilityNodeInfo.ACTION_CLEAR_FOCUS,
        AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
        AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
        AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY,
        AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
        AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT,
        AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT,
        AccessibilityNodeInfo.ACTION_SET_SELECTION,
        AccessibilityNodeInfo.ACTION_SET_TEXT,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_IN_DIRECTION.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_MOVE_WINDOW.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_PRESS_AND_HOLD.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_DRAG_START.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_DRAG_DROP.id,
        AccessibilityNodeInfo.AccessibilityAction.ACTION_DRAG_CANCEL.id
    )
}

internal class NodeActionExecutor(
    private val closeMenus: () -> Unit,
    private val showUnavailable: () -> Unit
) {
    fun execute(target: NodeActionTarget, actionId: Int) {
        val succeeded = target.perform(actionId)
        closeMenus()
        if (!succeeded) showUnavailable()
    }
}
