package com.enaboapps.switchify.service.remotebridge

import com.enaboapps.switchify.service.menu.structure.MenuConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwitchifyRemoteVisibilityPolicyTest {

    @Test
    fun showsEntriesWhenInstalledAndRemoteNotForeground() {
        assertTrue(SwitchifyRemoteVisibilityPolicy.shouldShowRemoteEntries(true, null))
        assertTrue(SwitchifyRemoteVisibilityPolicy.shouldShowRemoteEntries(true, "com.example.other"))
    }

    @Test
    fun hidesEntriesWhenRemoteIsForeground() {
        assertFalse(
            SwitchifyRemoteVisibilityPolicy.shouldShowRemoteEntries(
                true,
                SwitchifyRemoteLauncher.REMOTE_PACKAGE
            )
        )
    }

    @Test
    fun hidesEntriesWhenNotInstalled() {
        assertFalse(SwitchifyRemoteVisibilityPolicy.shouldShowRemoteEntries(false, null))
        assertFalse(SwitchifyRemoteVisibilityPolicy.shouldShowRemoteEntries(false, "com.example.other"))
        assertFalse(
            SwitchifyRemoteVisibilityPolicy.shouldShowRemoteEntries(
                false,
                SwitchifyRemoteLauncher.REMOTE_PACKAGE
            )
        )
    }

    @Test
    fun coversBothRemoteMenuItems() {
        assertEquals(
            setOf(
                MenuConstants.ItemIds.Main.CONTROL_PC,
                MenuConstants.ItemIds.Main.PC_SWITCH_FORWARDING
            ),
            SwitchifyRemoteVisibilityPolicy.REMOTE_ITEM_IDS
        )
    }
}
