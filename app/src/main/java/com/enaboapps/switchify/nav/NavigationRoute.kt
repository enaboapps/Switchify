package com.enaboapps.switchify.nav

sealed class NavigationRoute(val name: String) {

    object Home : NavigationRoute("Home")
    object Settings : NavigationRoute("Settings")

}
