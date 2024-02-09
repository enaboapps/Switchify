package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.models.SettingsScreenModel
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.PreferenceLink
import com.enaboapps.switchify.widgets.PreferenceSection
import com.enaboapps.switchify.widgets.PreferenceSwitch
import com.enaboapps.switchify.widgets.PreferenceTimeStepper

@Composable
fun SettingsScreen(navController: NavController) {
    val verticalScrollState = rememberScrollState()
    val context = LocalContext.current
    val settingsScreenModel = SettingsScreenModel(context)
    Scaffold(
        topBar = {
            NavBar(title = "Settings", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(it)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            PreferenceSection(title = "Scanning") {
                PreferenceLink(
                    title = "Scan Mode",
                    summary = "Configure the scan mode",
                    navController = navController,
                    route = NavigationRoute.ScanMode.name
                )
            }
            TimingSection(settingsScreenModel)
            SwitchStabilitySection(settingsScreenModel)
            SelectionSection(settingsScreenModel)
            PreferenceLink(
                title = "Switches",
                summary = "Configure your switches",
                navController = navController,
                route = NavigationRoute.Switches.name
            )
        }
    }
}


@Composable
private fun TimingSection(settingsScreenModel: SettingsScreenModel) {
    PreferenceSection(title = "Timing") {
        PreferenceTimeStepper(
            value = settingsScreenModel.scanRate.value ?: 0,
            title = "Scan rate",
            summary = "The interval at which the scanner will move to the next item",
            min = 200,
            max = 100000
        ) {
            settingsScreenModel.setScanRate(it)
        }
        PreferenceTimeStepper(
            value = settingsScreenModel.refineScanRate.value ?: 0,
            title = "Refine scan rate",
            summary = "The interval at which the scanner will move when refining the selection",
            min = 200,
            max = 100000
        ) {
            settingsScreenModel.setRefineScanRate(it)
        }
        PreferenceTimeStepper(
            value = settingsScreenModel.switchHoldTime.value ?: 0,
            title = "Switch hold time",
            summary = "The time to hold the switch before the long pressed action is triggered",
            min = 100,
            max = 100000
        ) {
            settingsScreenModel.setSwitchHoldTime(it)
        }
    }
}

// Switch Stability Section
@Composable
private fun SwitchStabilitySection(screenModel: SettingsScreenModel) {
    PreferenceSection(title = "Switch Stability") {
        PreferenceSwitch(
            title = "Pause scan on switch hold",
            summary = "Pause the scan when a switch is held",
            checked = screenModel.pauseScanOnSwitchHold.value ?: false,
            onCheckedChange = {
                screenModel.setPauseScanOnSwitchHold(it)
            }
        )
    }
}

@Composable
private fun SelectionSection(screenModel: SettingsScreenModel) {
    PreferenceSection(title = "Selection") {
        PreferenceSwitch(
            title = "Auto select",
            summary = "Automatically select the item after a delay",
            checked = screenModel.autoSelect.value ?: false,
            onCheckedChange = {
                screenModel.setAutoSelect(it)
            }
        )
        PreferenceTimeStepper(
            value = screenModel.autoSelectDelay.value ?: 0,
            title = "Auto select delay",
            summary = "The delay before the item is selected",
            min = 100,
            max = 100000
        ) {
            screenModel.setAutoSelectDelay(it)
        }
    }
}