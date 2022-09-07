package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.models.SettingsScreenModel
import com.enaboapps.switchify.widgets.PreferenceLink
import com.enaboapps.switchify.widgets.PreferenceSection
import com.enaboapps.switchify.widgets.PreferenceTimeStepper

@Composable
fun SettingsScreen(navController: NavController) {
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(it),
            verticalArrangement = Arrangement.Top
        ) {
            TimingSection()
            PreferenceLink(
                title = "Switches",
                summary = "Configure your switches",
                navController = navController,
                route = NavigationRoute.Switches
            )
        }
    }
}




@Composable
private fun TimingSection() {
    val settingsScreenModel = SettingsScreenModel(LocalContext.current)

    PreferenceSection(title = "Timing") {
        PreferenceTimeStepper(
            value = settingsScreenModel.getScanRate(),
            title = "Scan rate",
            summary = "The interval at which the scanner will move to the next item",
            min = 100,
            max = 100000,
            onValueChanged = {
                settingsScreenModel.setScanRate(it)
            }
        )
    }
}