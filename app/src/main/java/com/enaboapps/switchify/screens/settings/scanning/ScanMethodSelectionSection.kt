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
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.widgets.Section

@Composable
fun ScanMethodSelectionSection() {
    val methods = listOf(
        ScanMethod.MethodType.CURSOR,
        ScanMethod.MethodType.RADAR,
        ScanMethod.MethodType.ITEM_SCAN
    )
    val preferenceManager = PreferenceManager(LocalContext.current)
    val currentMethod = MutableLiveData<String>()
    currentMethod.value =
        preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD)
    val currentMethodState = currentMethod.observeAsState()
    val setScanMethod = { method: String ->
        preferenceManager.setStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD, method)
        currentMethod.value = method
    }

    Section(title = "SCAN METHOD") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            // radio buttons for each method
            methods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentMethodState.value == method,
                        onClick = {
                            setScanMethod(method)
                        }
                    )
                    Text(text = "${ScanMethod.getName(method)} - ${ScanMethod.getDescription(method)}")
                }
            }
        }
    }
}