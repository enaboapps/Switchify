package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.widgets.Picker
import com.enaboapps.switchify.widgets.Section

@Composable
fun ScanModeSelectionSection() {
    val preferenceManager = PreferenceManager(LocalContext.current)
    var currentMode by remember {
        mutableStateOf(
            ScanMode.fromId(
                preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE)
            )
        )
    }

    val setScanMode = { mode: ScanMode ->
        preferenceManager.setStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE, mode.id)
        currentMode = mode
    }

    Section(title = "SCAN MODE") {
        Picker(
            title = "Select Scan Mode",
            selectedItem = currentMode,
            items = ScanMode.modes.toList(),
            onItemSelected = setScanMode,
            itemToString = { it.getModeName() },
            itemDescription = { it.getModeDescription() }
        )
    }
}