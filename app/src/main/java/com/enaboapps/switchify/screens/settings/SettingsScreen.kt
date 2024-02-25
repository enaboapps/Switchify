package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.screens.settings.models.SettingsScreenModel
import com.enaboapps.switchify.service.scanning.ScanMode
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
    val mode =
        ScanMode.fromId(PreferenceManager(context).getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
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
            if (mode.id == ScanMode.Modes.MODE_AUTO) {
                TimingSection(settingsScreenModel)
            }
            PreferenceLink(
                title = "Switch Stability",
                summary = "Configure switch stability settings",
                navController = navController,
                route = NavigationRoute.SwitchStability.name
            )
            SelectionSection(settingsScreenModel)
            PreferenceLink(
                title = "Switches",
                summary = "Configure your switches",
                navController = navController,
                route = NavigationRoute.Switches.name
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            PreferenceLink(
                title = "About",
                summary = "About the app",
                navController = navController,
                route = NavigationRoute.About.name
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
        PreferenceSwitch(
            title = "Pause on first item",
            summary = "Pause scanning when the first item is highlighted",
            checked = settingsScreenModel.pauseOnFirstItem.value ?: false
        ) {
            settingsScreenModel.setPauseOnFirstItem(it)
        }
        if (settingsScreenModel.pauseOnFirstItem.observeAsState().value == true) {
            PreferenceTimeStepper(
                value = settingsScreenModel.pauseOnFirstItemDelay.value ?: 0,
                title = "Pause on first item delay",
                summary = "The delay to pause on the first item",
                min = 100,
                max = 100000
            ) {
                settingsScreenModel.setPauseOnFirstItemDelay(it)
            }
        }
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
        PreferenceSwitch(
            title = "Assisted selection",
            summary = "Assist the user in selecting items by selecting the closest available item to where they tap",
            checked = screenModel.assistedSelection.value ?: false,
            onCheckedChange = {
                screenModel.setAssistedSelection(it)
            }
        )
    }
}