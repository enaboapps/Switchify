package com.enaboapps.switchify.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.widgets.PreferenceTimeStepper

@Composable
fun SettingsScreen() {
    val settingsScreenModel = SettingsScreenModel(LocalContext.current)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
            PreferenceTimeStepper(
                value = settingsScreenModel.getScanRate(),
                title = "Scan rate",
                min = 100,
                max = 1000,
                onValueChanged = {
                    settingsScreenModel.setScanRate(it)
                }
            )
        }
    }
}


class SettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    fun getScanRate(): Int {
        return preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }

    fun setScanRate(rate: Int) {
        preferenceManager.setIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
    }
}