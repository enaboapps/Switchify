package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.switches.models.EditSwitchScreenModel
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun EditSwitchScreen(
    navController: NavController,
    code: String
) {
    val editSwitchScreenModel = EditSwitchScreenModel(code, SwitchEventStore(LocalContext.current))
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            NavBar(
                title = "Edit ${editSwitchScreenModel.name.value}",
                navController = navController
            )
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(it)
        ) {
            SwitchActionPicker(title = "Press Action", action = editSwitchScreenModel.pressAction)
            SwitchActionPicker(
                title = "Long Press Action",
                action = editSwitchScreenModel.longPressAction
            )
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