package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.methods.cursor.CursorMode
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.Picker
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
    var currentMode by remember { mutableStateOf(CursorMode.getMode()) }

    val setCursorMode = { mode: String ->
        preferenceManager.setStringValue(PreferenceManager.PREFERENCE_KEY_CURSOR_MODE, mode)
        currentMode = mode
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
                Picker(
                    title = "Select Cursor Mode",
                    selectedItem = currentMode,
                    items = cursorModes,
                    onItemSelected = setCursorMode,
                    itemToString = { CursorMode.getModeName(it) },
                    itemDescription = { CursorMode.getModeDescription(it) }
                )
            }

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