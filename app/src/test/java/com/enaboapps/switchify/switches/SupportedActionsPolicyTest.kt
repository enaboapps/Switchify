package com.enaboapps.switchify.switches

import com.enaboapps.switchify.service.scanning.ScanMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedActionsPolicyTest {
    @Test
    fun pcSwitchForwardingIsSupportedInEveryScanMode() {
        listOf(
            ScanMode.Modes.MODE_AUTO,
            ScanMode.Modes.MODE_MANUAL
        ).forEach { mode ->
            assertTrue(
                SupportedActionsPolicy.supportedActionIdsForMode(mode)
                    .contains(SwitchAction.ACTION_PC_SWITCH_FORWARDING)
            )
        }
    }

    @Test
    fun unknownModeUsesAutoScanActions() {
        assertTrue(
            SupportedActionsPolicy.supportedActionIdsForMode("unknown")
                .contains(SwitchAction.ACTION_SELECT)
        )
    }

    @Test
    fun selectableActionsDropRemoteActionsWhenRemoteNotInstalled() {
        listOf(
            ScanMode.Modes.MODE_AUTO,
            ScanMode.Modes.MODE_MANUAL
        ).forEach { mode ->
            val supported = SupportedActionsPolicy.supportedActionIdsForMode(mode)
            val selectable = SupportedActionsPolicy.selectableActionIds(supported, false)

            assertFalse(selectable.contains(SwitchAction.ACTION_CONTROL_PC))
            assertFalse(selectable.contains(SwitchAction.ACTION_PC_SWITCH_FORWARDING))
            assertTrue(selectable.contains(SwitchAction.ACTION_SELECT))
        }
    }

    @Test
    fun selectableActionsKeepRemoteActionsWhenRemoteInstalled() {
        val supported = SupportedActionsPolicy.supportedActionIdsForMode(ScanMode.Modes.MODE_AUTO)

        assertEquals(supported, SupportedActionsPolicy.selectableActionIds(supported, true))
    }

    @Test
    fun supportedActionsStillReportRemoteActionsWhenRemoteNotInstalled() {
        val supported = SupportedActionsPolicy.supportedActionIdsForMode(ScanMode.Modes.MODE_AUTO)

        assertTrue(supported.contains(SwitchAction.ACTION_CONTROL_PC))
        assertTrue(supported.contains(SwitchAction.ACTION_PC_SWITCH_FORWARDING))
    }
}
