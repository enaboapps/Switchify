package com.enaboapps.switchify.service.remotebridge

import android.content.Context
import com.enaboapps.switchify.service.core.ServiceCore
import com.enaboapps.switchify.service.menu.structure.MenuConstants

object SwitchifyRemoteVisibilityPolicy {

    val REMOTE_ITEM_IDS = setOf(
        MenuConstants.ItemIds.Main.CONTROL_PC,
        MenuConstants.ItemIds.Main.PC_SWITCH_FORWARDING
    )

    fun shouldShowRemoteEntries(installed: Boolean, foregroundPackage: String?): Boolean {
        return installed && foregroundPackage != SwitchifyRemoteLauncher.REMOTE_PACKAGE
    }

    fun shouldShowRemoteEntries(context: Context): Boolean {
        return shouldShowRemoteEntries(
            installed = SwitchifyRemoteLauncher.isInstalled(context),
            foregroundPackage = ServiceCore.getScanningManager()?.currentForegroundPackage()
        )
    }
}
