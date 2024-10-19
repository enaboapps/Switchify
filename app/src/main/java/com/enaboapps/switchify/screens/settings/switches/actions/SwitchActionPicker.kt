package com.enaboapps.switchify.screens.settings.switches.actions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enaboapps.switchify.service.custom.actions.AppLaunchPicker
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchActionExtra
import com.enaboapps.switchify.utils.AppLauncher
import com.enaboapps.switchify.widgets.Picker

@Composable
fun SwitchActionPicker(
    title: String,
    switchAction: SwitchAction,
    modifier: Modifier = Modifier,
    onChange: (SwitchAction) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var currentAction by remember { mutableStateOf(switchAction) }

    Column(modifier = modifier) {
        if (currentAction.isExtraAvailable()) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Picker(
            title = title,
            selectedItem = currentAction,
            items = SwitchAction.actions,
            onItemSelected = { newAction ->
                currentAction = newAction
                onChange(newAction)
            },
            onDelete = onDelete,
            itemToString = { it.getActionName() },
            itemDescription = { it.getActionDescription() }
        )

        if (currentAction.isExtraAvailable()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Arrow",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                when (currentAction.id) {
                    SwitchAction.ACTION_OPEN_APP -> AppLaunchPicker(
                        initialApp = currentAction.extra?.let {
                            AppLauncher.AppInfo(
                                it.appName,
                                it.appPackage
                            )
                        },
                        onAppSelected = { appInfo ->
                            val updatedAction = currentAction.copy(
                                extra = SwitchActionExtra(
                                    appName = appInfo.displayName,
                                    appPackage = appInfo.packageName
                                )
                            )
                            currentAction = updatedAction
                            onChange(updatedAction)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
