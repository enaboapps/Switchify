package com.enaboapps.switchify.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.scanning.ScanModeSelectionSection
import com.enaboapps.switchify.switches.SwitchConfigInvalidBanner
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun SetupScreen(navController: NavController) {
    val context = LocalContext.current
    val setupScreenModel = SetupScreenModel(context)

    LaunchedEffect(Unit) {
        setupScreenModel.checkSwitches()
    }

    Scaffold(
        topBar = {
            NavBar(title = "Setup", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SetupScreenContent(
                setupScreenModel = setupScreenModel,
                isAccessibilityServiceEnabled = setupScreenModel.isAccessibilityServiceEnabled.value == true,
                isSwitchifyKeyboardEnabled = setupScreenModel.isSwitchifyKeyboardEnabled.value == true,
                onEditSwitchesClick = {
                    navController.navigate(NavigationRoute.Switches.name)
                },
                onEnableAccessibilityServiceClick = {
                    navController.navigate(NavigationRoute.EnableAccessibilityService.name)
                },
                onEnableSwitchifyKeyboardClick = {
                    navController.navigate(NavigationRoute.EnableSwitchifyKeyboard.name)
                },
                onFinishClick = {
                    setupScreenModel.setSetupComplete(context)
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun SetupScreenContent(
    setupScreenModel: SetupScreenModel,
    isAccessibilityServiceEnabled: Boolean,
    isSwitchifyKeyboardEnabled: Boolean,
    onEditSwitchesClick: () -> Unit,
    onEnableAccessibilityServiceClick: () -> Unit,
    onEnableSwitchifyKeyboardClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val observeSwitchesInvalid = setupScreenModel.switchesInvalid.observeAsState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Switchify!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        if (observeSwitchesInvalid.value != null) {
            ScanModeSelectionSection(onChange = {
                setupScreenModel.checkSwitches()
            })
            SwitchConfigInvalidBanner(observeSwitchesInvalid.value)
            FullWidthButton(text = "Edit Switches", onClick = onEditSwitchesClick)
            FullWidthButton(text = "I'll Skip The Setup", onClick = onFinishClick)
        } else if (!isAccessibilityServiceEnabled) {
            Text(
                text = "To use Switchify effectively, please enable the Accessibility Service.",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            val context = LocalContext.current
            val disclosure = context.resources.getString(R.string.accessibility_service_disclosure)
            Text(
                text = disclosure,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Let's Go", onClick = onEnableAccessibilityServiceClick)
            FullWidthButton(text = "I'll Skip The Setup", onClick = onFinishClick)
        } else if (!isSwitchifyKeyboardEnabled) {
            Text(
                text = "To use Switchify effectively, please enable the Switchify Keyboard in your device settings.",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Let's Go", onClick = onEnableSwitchifyKeyboardClick)
            FullWidthButton(text = "I'll Skip The Setup", onClick = onFinishClick)
        } else {
            Text(
                text = "You're all set up!",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Finish", onClick = onFinishClick)
        }
    }
}