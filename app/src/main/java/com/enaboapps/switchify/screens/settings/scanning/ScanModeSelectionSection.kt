package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.widgets.Section

@Composable
fun ScanModeSelectionSection() {
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

    Section(title = "SCAN MODE") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                    Text(text = "${mode.getModeName()} - ${mode.getModeDescription()}")
                }
            }
        }
    }
}