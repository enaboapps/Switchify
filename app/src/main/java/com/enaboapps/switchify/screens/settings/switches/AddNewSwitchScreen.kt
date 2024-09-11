package com.enaboapps.switchify.screens.settings.switches

import android.widget.Toast
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.enaboapps.switchify.service.utils.ServiceUtils
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun AddNewSwitchScreen(navController: NavController) {
    val context = LocalContext.current
    val switchEventStore = SwitchEventStore(context)
    val addNewSwitchScreenModel = remember {
        AddNewSwitchScreenModel(
            context = context,
            store = switchEventStore
        )
    }
    val verticalScrollState = rememberScrollState()
    val shouldSave by addNewSwitchScreenModel.shouldSave.observeAsState()
    val serviceUtils = ServiceUtils()
    val isServiceEnabled = serviceUtils.isAccessibilityServiceEnabled(context)
    LaunchedEffect(isServiceEnabled) {
        // If the service is enabled, show a warning and pop back to the previous screen
        if (isServiceEnabled) {
            Toast.makeText(
                context,
                "Please disable the Switchify service before adding a new switch",
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }
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
                    Text(text = "Switch captured", style = MaterialTheme.typography.labelMedium)
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
            Text(
                text = "Is your switch not working? " +
                        "If you are using a USB switch, please make sure that you have it plugged in and that it is turned on. " +
                        "If you are using a Bluetooth switch, please make sure that it is paired with your device.",
                style = MaterialTheme.typography.bodyMedium
            )
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
        Text(text = "Activate your switch", style = MaterialTheme.typography.titleMedium)
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
        Text(text = name.value!!, style = MaterialTheme.typography.titleMedium)
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