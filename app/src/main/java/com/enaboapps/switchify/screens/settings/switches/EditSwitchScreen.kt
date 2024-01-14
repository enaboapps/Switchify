package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.switches.models.EditSwitchScreenModel
import com.enaboapps.switchify.switches.SwitchEventStore

@Composable
fun EditSwitchScreen(
    navController: NavController,
    code: String
) {
    val editSwitchScreenModel = EditSwitchScreenModel(code, SwitchEventStore(LocalContext.current))
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Edit ${editSwitchScreenModel.name.value}") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(it)
        ) {
            SwitchActionPicker(title = "Press Action", action = editSwitchScreenModel.pressAction)
            SwitchActionPicker(title = "Long Press Action", action = editSwitchScreenModel.longPressAction)
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    editSwitchScreenModel.save()
                    navController.popBackStack()
                }
            ) {
                Text(text = "Save")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    editSwitchScreenModel.delete {
                        navController.popBackStack()
                    }
                }
            ) {
                Text(text = "Delete")
            }
        }
    }
}