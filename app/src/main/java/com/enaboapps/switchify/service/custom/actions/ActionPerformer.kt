package com.enaboapps.switchify.service.custom.actions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_COPY_TEXT_TO_CLIPBOARD
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
            ACTION_COPY_TEXT_TO_CLIPBOARD -> copyTextToClipboard(
                action.extra?.textToCopy ?: ""
            ) // Copy text to clipboard
        }
    }

    private fun openApp(appPackage: String) {
        val appLauncher = AppLauncher(context)
        appLauncher.launchAppByPackageName(appPackage)
    }

    private fun copyTextToClipboard(textToCopy: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
    }
}