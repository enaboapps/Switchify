package com.enaboapps.switchify.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun SetupScreen(navController: NavController) {
    val context = LocalContext.current
    val setupScreenModel = SetupScreenModel(context)
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
                switchCount = setupScreenModel.switchCount.value ?: 0,
                isAccessibilityServiceEnabled = setupScreenModel.isAccessibilityServiceEnabled.value
                    ?: false,
                onAddSwitchClick = {
                    navController.navigate(NavigationRoute.AddNewSwitch.name)
                },
                onEnableAccessibilityServiceClick = {
                    navController.navigate(NavigationRoute.EnableAccessibilityService.name)
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
    switchCount: Int,
    isAccessibilityServiceEnabled: Boolean,
    onAddSwitchClick: () -> Unit,
    onEnableAccessibilityServiceClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Switchify!",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        if (switchCount == 0) {
            Text(
                text = "To get started, please add a switch.",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Add Switch", onClick = onAddSwitchClick)
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
        } else {
            Text(
                text = "You're all set up!",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Finish", onClick = onFinishClick)
        }
    }
}