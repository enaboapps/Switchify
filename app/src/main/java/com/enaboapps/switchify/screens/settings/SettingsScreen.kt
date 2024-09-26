package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
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
import com.enaboapps.switchify.widgets.NavRouteLink
import com.enaboapps.switchify.widgets.PreferenceSwitch
import com.enaboapps.switchify.widgets.PreferenceTimeStepper
import com.enaboapps.switchify.widgets.Section

@Composable
fun SettingsScreen(navController: NavController) {
    val verticalScrollState = rememberScrollState()
    val context = LocalContext.current
    val settingsScreenModel = SettingsScreenModel(context)
    val mode =
        ScanMode.fromId(PreferenceManager(context).getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
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
            NavRouteLink(
                title = "Switches",
                summary = "Configure your switches",
                navController = navController,
                route = NavigationRoute.Switches.name
            )
            Section(title = "Scanning") {
                NavRouteLink(
                    title = "Scan Mode",
                    summary = "Configure the scan mode",
                    navController = navController,
                    route = NavigationRoute.ScanMode.name
                )
                Spacer(modifier = Modifier.padding(top = 16.dp))
                NavRouteLink(
                    title = "Scan Method",
                    summary = "Configure the scan method",
                    navController = navController,
                    route = NavigationRoute.ScanMethod.name
                )
                Spacer(modifier = Modifier.padding(top = 16.dp))
                NavRouteLink(
                    title = "Scan Color",
                    summary = "Configure the scan color",
                    navController = navController,
                    route = NavigationRoute.ScanColor.name
                )
            }
            CursorSection(navController)
            if (mode.id == ScanMode.Modes.MODE_AUTO) {
                TimingSection(settingsScreenModel)
            }
            NavRouteLink(
                title = "Switch Stability",
                summary = "Configure switch stability settings",
                navController = navController,
                route = NavigationRoute.SwitchStability.name
            )
            SelectionSection(settingsScreenModel)
            ItemScanSection(settingsScreenModel)
            KeyboardSection(navController)
            Spacer(modifier = Modifier.padding(top = 16.dp))
            NavRouteLink(
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
    Section(title = "Timing") {
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
            value = settingsScreenModel.radarScanRate.value ?: 0,
            title = "Radar scan rate",
            summary = "The interval at which the radar will move",
            min = 10,
            max = 100000,
            step = 10
        ) {
            settingsScreenModel.setRadarScanRate(it)
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
private fun KeyboardSection(navController: NavController) {
    Section(title = "Keyboard") {
        NavRouteLink(
            title = "Choose Prediction Language",
            summary = "Choose the prediction language",
            navController = navController,
            route = NavigationRoute.PredictionLanguage.name
        )
    }
}

@Composable
private fun CursorSection(navController: NavController) {
    Section(title = "Cursor") {
        NavRouteLink(
            title = "Cursor Mode",
            summary = "Configure the cursor mode",
            navController = navController,
            route = NavigationRoute.CursorMode.name
        )
    }
}

@Composable
private fun SelectionSection(screenModel: SettingsScreenModel) {
    Section(title = "Selection") {
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

@Composable
private fun ItemScanSection(screenModel: SettingsScreenModel) {
    Section(title = "Item Scan") {
        PreferenceSwitch(
            title = "Row column scan",
            summary = "Scan items in a row column pattern",
            checked = screenModel.rowColumnScan.value ?: false,
            onCheckedChange = {
                screenModel.setRowColumnScan(it)
            }
        )
        PreferenceSwitch(
            title = "Group scan (requires row column scan)",
            summary = "Scan items in a group pattern",
            checked = screenModel.groupScan.value ?: false,
            onCheckedChange = {
                screenModel.setGroupScan(it)
            }
        )
    }
}