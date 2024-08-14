package com.enaboapps.switchify.screens.howto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.widgets.InfoCard
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.PreferenceLink
import com.enaboapps.switchify.widgets.PreferenceSection

@Composable
fun HowToUseScreen(navController: NavController) {
    Scaffold(
        topBar = {
            NavBar(title = "How To Use", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PreferenceSection(title = "Step 1") {
                PreferenceLink(
                    title = "Add Switches",
                    summary = "Add your switches to the app",
                    navController = navController,
                    route = NavigationRoute.Switches.name
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
                InfoCard(
                    title = "What are switches?",
                    description = "Switches are physical buttons that are connected to your device. You can add them to the app and use them to control your device."
                )
            }
            PreferenceSection(title = "Step 2") {
                PreferenceLink(
                    title = "Enable Accessibility Service",
                    summary = "Enable the Switchify Accessibility Service",
                    navController = navController,
                    route = NavigationRoute.EnableAccessibilityService.name
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
                InfoCard(
                    title = "What is the Accessibility Service?",
                    description = "The Accessibility Service is a system service that allows Switchify to detect when you press a switch."
                )
            }
            PreferenceSection(title = "Step 3") {
                PreferenceLink(
                    title = "Enable Keyboard",
                    summary = "Enable the Switchify Keyboard",
                    navController = navController,
                    route = NavigationRoute.EnableSwitchifyKeyboard.name
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
                InfoCard(
                    title = "What is the Switchify Keyboard?",
                    description = "The Switchify Keyboard is a virtual keyboard that allows you to control your device using your switches."
                )
            }
        }
    }
}