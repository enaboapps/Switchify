package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.widgets.Picker
import com.enaboapps.switchify.widgets.Section

@Composable
fun ScanMethodSelectionSection() {
    val methods = listOf(
        ScanMethod.MethodType.CURSOR,
        ScanMethod.MethodType.RADAR,
        ScanMethod.MethodType.ITEM_SCAN
    )
    val preferenceManager = PreferenceManager(LocalContext.current)
    var currentMethod by remember {
        mutableStateOf(
            preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD)
        )
    }

    val setScanMethod = { method: String ->
        preferenceManager.setStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD, method)
        currentMethod = method
    }

    Section(title = "SCAN METHOD") {
        Picker(
            title = "Select Scan Method",
            selectedItem = currentMethod,
            items = methods,
            onItemSelected = setScanMethod,
            itemToString = { ScanMethod.getName(it) },
            itemDescription = { ScanMethod.getDescription(it) }
        )
    }
}