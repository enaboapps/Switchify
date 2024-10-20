package com.enaboapps.switchify.service.custom.actions

import android.content.Context
import com.enaboapps.switchify.service.custom.actions.data.ACTION_OPEN_APP
import com.enaboapps.switchify.service.menu.store.MenuItemJsonStore
import com.enaboapps.switchify.utils.AppLauncher

class ActionPerformer(
    private val context: Context
) {
    private val actionJsonStore = MenuItemJsonStore(context)

    fun performAction(actionId: String) {
        val action = actionJsonStore.getMenuItem(actionId)
        when (action?.action) {
            ACTION_OPEN_APP -> openApp(action.extra?.appPackage ?: "")
        }
    }

    private fun openApp(appPackage: String) {
        val appLauncher = AppLauncher(context)
        appLauncher.launchAppByPackageName(appPackage)
    }
}