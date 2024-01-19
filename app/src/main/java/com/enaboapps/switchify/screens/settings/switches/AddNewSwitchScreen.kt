package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.settings.switches.models.AddNewSwitchScreenModel
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun AddNewSwitchScreen(navController: NavController) {
    val addNewSwitchScreenModel = AddNewSwitchScreenModel(SwitchEventStore(LocalContext.current))
    val verticalScrollState = rememberScrollState()
    val shouldSave by addNewSwitchScreenModel.shouldSave.observeAsState()
    Scaffold(
        topBar = {
            NavBar(title = "Add New Switch", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(verticalScrollState)
                .padding(it)
                .padding(all = 16.dp),
        ) {
            SwitchName(name = addNewSwitchScreenModel.name)
            if (!shouldSave!!) {
                SwitchListener(onKeyEvent = { keyEvent: KeyEvent ->
                    addNewSwitchScreenModel.processKeyCode(keyEvent.key)
                })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Switch captured", style = MaterialTheme.typography.h5)
                    SwitchActionSection(
                        viewModel = addNewSwitchScreenModel,
                        modifier = Modifier.padding(16.dp)
                    )
                    FullWidthButton(text = "Save", onClick = {
                        addNewSwitchScreenModel.save()
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}

@Composable
fun SwitchListener(onKeyEvent: (KeyEvent) -> Unit) {
    val requester = remember { FocusRequester() }
    Row(modifier = Modifier
        .padding(16.dp)
        .onKeyEvent { keyEvent ->
            onKeyEvent(keyEvent)
            true
        }
        .fillMaxWidth()
        .focusRequester(requester)
        .focusable(),
        horizontalArrangement = Arrangement.Center) {
        Text(text = "Activate your switch", style = MaterialTheme.typography.h5)
    }
    LaunchedEffect(requester) {
        requester.requestFocus()
    }
}

@Composable
fun SwitchName(name: MutableLiveData<String>) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = name.value!!, style = MaterialTheme.typography.h5)
    }
}

@Composable
fun SwitchActionSection(
    viewModel: AddNewSwitchScreenModel,
    modifier: Modifier = Modifier
) {
    Column {
        SwitchActionPicker(
            title = "Press action",
            action = viewModel.pressAction,
            modifier = modifier
        )
        SwitchActionPicker(
            title = "Long press action",
            action = viewModel.longPressAction,
            modifier = modifier
        )
    }
}