package com.enaboapps.switchify.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enaboapps.switchify.screens.EnableAccessibilityServiceScreen
import com.enaboapps.switchify.screens.HomeScreen
import com.enaboapps.switchify.screens.account.SignInScreen
import com.enaboapps.switchify.screens.account.SignUpScreen
import com.enaboapps.switchify.screens.settings.switches.AddNewSwitchScreen
import com.enaboapps.switchify.screens.settings.SettingsScreen
import com.enaboapps.switchify.screens.settings.scanning.ScanModeSelectionScreen
import com.enaboapps.switchify.screens.settings.switches.EditSwitchScreen
import com.enaboapps.switchify.screens.settings.switches.SwitchesScreen
import com.enaboapps.switchify.screens.setup.SetupScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = NavigationRoute.Home.name) {
        composable(NavigationRoute.Home.name) {
            HomeScreen(navController)
        }
        composable(NavigationRoute.Setup.name) {
            SetupScreen(navController)
        }
        composable(NavigationRoute.SignIn.name) {
            SignInScreen(navController)
        }
        composable(NavigationRoute.SignUp.name) {
            SignUpScreen(navController)
        }
        composable(NavigationRoute.Settings.name) {
            SettingsScreen(navController)
        }
        composable(NavigationRoute.ScanMode.name) {
            ScanModeSelectionScreen(navController)
        }
        composable(NavigationRoute.Switches.name) {
            SwitchesScreen(navController)
        }
        composable(NavigationRoute.AddNewSwitch.name) {
            AddNewSwitchScreen(navController)
        }
        composable("${NavigationRoute.EditSwitch.name}/{code}") {
            it.arguments?.getString("code")?.let { code ->
                EditSwitchScreen(navController, code)
            }
        }
        composable(NavigationRoute.EnableAccessibilityService.name) {
            EnableAccessibilityServiceScreen(navController)
        }
    }
}