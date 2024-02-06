package com.enaboapps.switchify.nav

sealed class NavigationRoute(val name: String) {

    data object Home : NavigationRoute("Home")
    data object Setup : NavigationRoute("Setup")
    data object SignIn : NavigationRoute("SignIn")
    data object SignUp : NavigationRoute("SignUp")
    data object ForgotPassword : NavigationRoute("ForgotPassword")
    data object Account : NavigationRoute("Account")
    data object ChangePassword : NavigationRoute("ChangePassword")
    data object Settings : NavigationRoute("Settings")
    data object ScanMode : NavigationRoute("ScanMode")
    data object Switches : NavigationRoute("Switches")
    data object AddNewSwitch : NavigationRoute("AddNewSwitch")
    data object EditSwitch : NavigationRoute("EditSwitch")
    data object EnableAccessibilityService : NavigationRoute("EnableAccessibilityService")

}
