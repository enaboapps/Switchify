package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.switches.actions.SwitchActionPicker
import com.enaboapps.switchify.screens.settings.switches.models.EditSwitchScreenModel
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun EditSwitchScreen(
    navController: NavController,
    code: String
) {
    val editSwitchScreenModel = EditSwitchScreenModel(code, SwitchEventStore(LocalContext.current))
    val observeLongPressActions = editSwitchScreenModel.longPressActions.observeAsState()
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            NavBar(
                title = "Edit ${editSwitchScreenModel.name.value}",
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(paddingValues)
        ) {
            editSwitchScreenModel.pressAction.value?.let { press ->
                SwitchActionPicker(title = "Press Action", switchAction = press, onChange = {
                    editSwitchScreenModel.pressAction.value = it
                })
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Text(
                text = "Each switch can have multiple actions for long press. " +
                        "You can add or remove actions below. " +
                        "The actions will be executed in the order they are listed based on the duration of the long press.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            observeLongPressActions.value?.forEach { action ->
                SwitchActionPicker(
                    title = "Long Press Action ${observeLongPressActions.value!!.indexOf(action) + 1}",
                    switchAction = action,
                    onChange = { newAction ->
                        editSwitchScreenModel.updateLongPressAction(action, newAction)
                    },
                    onDelete = {
                        editSwitchScreenModel.removeLongPressAction(action)
                    }
                )
            }
            FullWidthButton(text = "Add Long Press Action", onClick = {
                editSwitchScreenModel.addLongPressAction(SwitchAction(SwitchAction.ACTION_SELECT))
            })
            FullWidthButton(text = "Save", onClick = {
                editSwitchScreenModel.save {
                    navController.popBackStack()
                }
            })
            FullWidthButton(text = "Delete", onClick = {
                editSwitchScreenModel.delete {
                    navController.popBackStack()
                }
            })
        }
    }
}