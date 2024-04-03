package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.cursor.CursorMode
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun CursorModeSelectionScreen(navController: NavController) {
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
            NavBar(title = "Cursor Mode", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .padding(all = 16.dp),
        ) {
            // radio buttons for each mode
            cursorModes.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentModeState.value == mode,
                        onClick = {
                            setCursorMode(mode)
                        }
                    )
                    Text(text = CursorMode.getModeName(mode))
                }
            }

            // show the current mode info
            CursorModeInfo(mode = currentModeState.value ?: CursorMode.Modes.MODE_SINGLE)
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