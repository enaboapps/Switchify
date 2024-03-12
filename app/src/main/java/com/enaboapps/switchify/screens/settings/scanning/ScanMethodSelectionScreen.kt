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
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun ScanMethodSelectionScreen(navController: NavController) {
    val methods = listOf(ScanMethod.MethodType.CURSOR, ScanMethod.MethodType.ITEM_SCAN)
    val preferenceManager = PreferenceManager(LocalContext.current)
    val currentMethod = MutableLiveData<Int>()
    currentMethod.value =
        preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD)
    val currentMethodState = currentMethod.observeAsState()
    val setScanMethod = { method: Int ->
        preferenceManager.setIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_METHOD, method)
        currentMethod.value = method
    }
    Scaffold(
        topBar = {
            NavBar(title = "Scan Method", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .padding(all = 16.dp),
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
                    Text(text = ScanMethod.getName(method))
                }
            }

            // show the current method info
            ScanMethodInfo(method = currentMethodState.value ?: ScanMethod.MethodType.CURSOR)
        }
    }
}

@Composable
fun ScanMethodInfo(method: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp)
    ) {
        Text(text = ScanMethod.getName(method))
        Text(text = ScanMethod.getDescription(method))
    }
}