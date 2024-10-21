package com.enaboapps.switchify.service.custom.actions

import android.content.Context
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_OPEN_APP
import com.enaboapps.switchify.utils.AppLauncher

class ActionPerformer(
    private val context: Context
) {
    private val actionStore = ActionStore(context)

    fun performAction(actionId: String) {
        val action = actionStore.getAction(actionId)
        when (action?.action) {
            ACTION_OPEN_APP -> openApp(action.extra?.appPackage ?: "") // Open app
        }
    }

    private fun openApp(appPackage: String) {
        val appLauncher = AppLauncher(context)
        appLauncher.launchAppByPackageName(appPackage)
    }
}