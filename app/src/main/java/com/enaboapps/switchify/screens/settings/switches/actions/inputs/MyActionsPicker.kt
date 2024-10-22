package com.enaboapps.switchify.screens.settings.switches.actions.inputs

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchActionExtra
import com.enaboapps.switchify.widgets.Picker

@Composable
fun MyActionsPicker(
    currentAction: SwitchAction,
    onChange: (SwitchAction) -> Unit
) {
    val context = LocalContext.current
    val actionStore = ActionStore(context)
    val actions = actionStore.getActions()

    Picker(
        title = "Select My Action",
        selectedItem = currentAction,
        items = actions.map { action ->
            SwitchAction(
                id = SwitchAction.ACTION_PERFORM_USER_ACTION,
                extra = SwitchActionExtra(
                    myActionsId = action.id,
                    myActionName = action.text
                )
            )
        },
        onItemSelected = onChange,
        itemToString = { it.extra?.myActionName ?: "" },
        itemDescription = { "Perform this action" }
    )
}
