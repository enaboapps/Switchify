package com.enaboapps.switchify.screens.settings.switches.actions.extras

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchActionExtra
import com.enaboapps.switchify.utils.AppLauncher
import com.enaboapps.switchify.widgets.Picker

@Composable
fun SwitchActionAppLaunchPicker(
    switchAction: SwitchAction,
    onAppSelected: (SwitchAction) -> Unit
) {
    val context = LocalContext.current
    val appLauncher = remember { AppLauncher(context) }
    val allApps = remember { appLauncher.getInstalledApps() }

    val selectedAppInfo = remember(switchAction.extra) {
        switchAction.extra?.appName?.let { appName ->
            allApps.find { it.displayName == appName }
        }
    }

    Picker(
        title = "Select App",
        selectedItem = selectedAppInfo,
        items = allApps,
        onItemSelected = { app ->
            val updatedAction = switchAction.copy(
                extra = SwitchActionExtra(appName = app.displayName, appPackage = app.packageName)
            )
            onAppSelected(updatedAction)
        },
        itemToString = { it.displayName },
        itemDescription = { "Will open ${it.displayName}" }
    )
}