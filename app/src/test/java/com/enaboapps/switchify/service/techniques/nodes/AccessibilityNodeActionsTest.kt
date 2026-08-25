package com.enaboapps.switchify.service.techniques.nodes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityNodeActionsTest {
    @Test
    fun resolvesUsefulAndCustomActionsInReportedOrder() {
        val actions = listOf(
            ReportedNodeAction(1, null),
            ReportedNodeAction(9001, "Archive"),
            ReportedNodeAction(2, "Open details"),
            ReportedNodeAction(1, null)
        )

        val result = NodeActionPolicy.resolve(
            actions = actions,
            standardLabel = { id -> mapOf(1 to "Click", 2 to "Long click")[id] },
            isExcluded = { false }
        )

        assertEquals(
            listOf(
                NodeActionDescriptor(1, "Click"),
                NodeActionDescriptor(9001, "Archive"),
                NodeActionDescriptor(2, "Open details")
            ),
            result
        )
    }

    @Test
    fun excludesInternalAndUnlabelledUnknownActions() {
        val result = NodeActionPolicy.resolve(
            actions = listOf(
                ReportedNodeAction(10, "Accessibility focus"),
                ReportedNodeAction(11, null),
                ReportedNodeAction(12, "Custom action")
            ),
            standardLabel = { null },
            isExcluded = { it == 10 }
        )

        assertEquals(listOf(NodeActionDescriptor(12, "Custom action")), result)
    }

    @Test
    fun choosesSmallestActionableNodeContainingPoint() {
        val selected = NodeActionTargetSelector.selectSmallest(
            x = 50f,
            y = 50f,
            candidates = listOf(
                NodeActionCandidate("parent", 0, 0, 100, 100, traversalOrder = 0),
                NodeActionCandidate("child", 25, 25, 50, 50, traversalOrder = 1),
                NodeActionCandidate("outside", 200, 200, 20, 20, traversalOrder = 2)
            )
        )

        assertEquals("child", selected)
    }

    @Test
    fun choosesLaterNodeWhenBoundsMatch() {
        val selected = NodeActionTargetSelector.selectSmallest(
            x = 50f,
            y = 50f,
            candidates = listOf(
                NodeActionCandidate("parent", 0, 0, 100, 100, traversalOrder = 0),
                NodeActionCandidate("child", 0, 0, 100, 100, traversalOrder = 1)
            )
        )

        assertEquals("child", selected)
    }

    @Test
    fun returnsNullWhenNoActionableNodeContainsPoint() {
        assertNull(
            NodeActionTargetSelector.selectSmallest(
                x = 50f,
                y = 50f,
                candidates = listOf(NodeActionCandidate("outside", 100, 100, 20, 20))
            )
        )
    }

    @Test
    fun executorPerformsBeforeClosingAndReportsFailure() {
        val events = mutableListOf<String>()
        val target = NodeActionTarget(
            actions = listOf(NodeActionDescriptor(1, "Click")),
            actionPerformer = {
                events += "perform"
                false
            }
        )
        val executor = NodeActionExecutor(
            closeMenus = { events += "close" },
            showUnavailable = { events += "unavailable" }
        )

        executor.execute(target, 1)

        assertEquals(listOf("perform", "close", "unavailable"), events)
        assertFalse(target.perform(1))
    }

    @Test
    fun executorDoesNotReportSuccessfulActionAsUnavailable() {
        var unavailable = false
        var closed = false
        val target = NodeActionTarget(emptyList()) { true }
        val executor = NodeActionExecutor(
            closeMenus = { closed = true },
            showUnavailable = { unavailable = true }
        )

        executor.execute(target, 1)

        assertTrue(closed)
        assertFalse(unavailable)
    }
}
