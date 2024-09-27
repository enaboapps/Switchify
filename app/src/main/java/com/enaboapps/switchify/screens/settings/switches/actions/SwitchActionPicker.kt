package com.enaboapps.switchify.screens.settings.switches.actions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.widgets.Picker

@Composable
fun SwitchActionPicker(
    title: String,
    switchAction: SwitchAction,
    modifier: Modifier = Modifier,
    onChange: ((SwitchAction) -> Unit),
    onDelete: (() -> Unit)? = null
) {
    Picker(
        title = title,
        selectedItem = switchAction,
        items = SwitchAction.actions.toList(),
        modifier = modifier,
        onItemSelected = onChange,
        onDelete = onDelete,
        itemToString = { it.getActionName() },
        itemDescription = { it.getActionDescription() }
    )
}