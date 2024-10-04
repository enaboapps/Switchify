package com.enaboapps.switchify.screens.settings.switches.actions

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.enaboapps.switchify.screens.settings.switches.actions.extras.SwitchActionAppLaunchPicker
import com.enaboapps.switchify.switches.SwitchAction
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
            when (currentAction.id) {
                SwitchAction.ACTION_OPEN_APP -> SwitchActionAppLaunchPicker(
                    switchAction = currentAction,
                    onAppSelected = { newAction ->
                        currentAction = newAction
                        onChange(newAction)
                    }
                )
            }
        }
    }
}