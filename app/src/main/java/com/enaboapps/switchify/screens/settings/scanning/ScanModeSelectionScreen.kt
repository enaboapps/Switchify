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
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun ScanModeSelectionScreen(navController: NavController) {
    val modes = ScanMode.modes
    val preferenceManager = PreferenceManager(LocalContext.current)
    val currentMode = MutableLiveData<ScanMode>()
    currentMode.value =
        ScanMode.fromId(preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
    val currentModeState = currentMode.observeAsState()
    val setScanMode = { mode: ScanMode ->
        preferenceManager.setStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE, mode.id)
        currentMode.value = mode
    }
    Scaffold(
        topBar = {
            NavBar(title = "Scan Mode", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .padding(all = 16.dp),
        ) {
            // radio buttons for each mode
            modes.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentModeState.value == mode,
                        onClick = {
                            setScanMode(mode)
                        }
                    )
                    Text(text = mode.getModeName())
                }
            }

            // show the current mode info
            ScanModeInfo(mode = currentModeState.value ?: ScanMode.modes[0])
        }
    }
}

@Composable
fun ScanModeInfo(mode: ScanMode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Text(text = mode.getModeName())
        Text(text = mode.getModeDescription())
    }
}