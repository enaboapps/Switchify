package com.enaboapps.switchify.service.custom.actions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.enaboapps.switchify.utils.AppLauncher
import com.enaboapps.switchify.widgets.Picker

@Composable
fun AppLaunchPicker(
    initialApp: AppLauncher.AppInfo? = null,
    onAppSelected: (AppLauncher.AppInfo) -> Unit,
    onNoAppSelected: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val appLauncher = remember { AppLauncher(context) }
    val allApps = remember { appLauncher.getInstalledApps() }

    LaunchedEffect(initialApp) {
        if (initialApp == null) {
            onNoAppSelected?.invoke()
        }
    }

    Picker(
        title = "Select App",
        selectedItem = initialApp,
        items = allApps,
        onItemSelected = { app ->
            onAppSelected(app)
        },
        itemToString = { it.displayName },
        itemDescription = { "Will open ${it.displayName}" }
    )
}
