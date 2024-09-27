package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.methods.cursor.CursorMode
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.PreferenceTimeStepper
import com.enaboapps.switchify.widgets.Section

@Composable
fun CursorSettingsScreen(navController: NavController) {
    CursorMode.init(LocalContext.current)
    val cursorModes = listOf(
        CursorMode.Modes.MODE_SINGLE,
        CursorMode.Modes.MODE_BLOCK
    )
    val preferenceManager = PreferenceManager(LocalContext.current)
    val currentMode = MutableLiveData<String>()
    currentMode.value = CursorMode.getMode()
    val currentModeState = currentMode.observeAsState()

    val setCursorMode = { mode: String ->
        preferenceManager.setStringValue(PreferenceManager.PREFERENCE_KEY_CURSOR_MODE, mode)
        currentMode.value = mode
    }

    Scaffold(
        topBar = {
            NavBar(title = "Cursor Settings", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(vertical = 16.dp),
        ) {
            Section(title = "Cursor Mode") {
                cursorModes.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentModeState.value == mode,
                            onClick = { setCursorMode(mode) }
                        )
                        Text(text = CursorMode.getModeName(mode))
                    }
                }
            }

            // show the current mode info
            CursorModeInfo(mode = currentModeState.value ?: CursorMode.Modes.MODE_SINGLE)

            Section(title = "Cursor Scan Rates") {
                PreferenceTimeStepper(
                    value = preferenceManager.getLongValue(
                        PreferenceManager.PREFERENCE_KEY_CURSOR_FINE_SCAN_RATE,
                        1000
                    ),
                    title = "Fine Cursor Scan Rate",
                    summary = "Adjust the scan rate for fine cursor movements",
                    min = 100,
                    max = 5000,
                    step = 100,
                    onValueChanged = { newValue ->
                        preferenceManager.setLongValue(
                            PreferenceManager.PREFERENCE_KEY_CURSOR_FINE_SCAN_RATE,
                            newValue
                        )
                    }
                )

                PreferenceTimeStepper(
                    value = preferenceManager.getLongValue(
                        PreferenceManager.PREFERENCE_KEY_CURSOR_BLOCK_SCAN_RATE,
                        1000
                    ),
                    title = "Block Cursor Scan Rate",
                    summary = "Adjust the scan rate for block cursor movements",
                    min = 100,
                    max = 5000,
                    step = 100,
                    onValueChanged = { newValue ->
                        preferenceManager.setLongValue(
                            PreferenceManager.PREFERENCE_KEY_CURSOR_BLOCK_SCAN_RATE,
                            newValue
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CursorModeInfo(mode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Text(text = CursorMode.getModeName(mode))
        Text(text = CursorMode.getModeDescription(mode))
    }
}