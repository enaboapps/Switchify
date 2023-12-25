package com.enaboapps.switchify.nav

sealed class NavigationRoute(val name: String) {

    object Home : NavigationRoute("Home")
    object Settings : NavigationRoute("Settings")
    object Switches : NavigationRoute("Switches")
    object AddNewSwitch : NavigationRoute("AddNewSwitch")
    object EnableAccessibilityService : NavigationRoute("EnableAccessibilityService")

}
