package com.enaboapps.switchify.widgets

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute

@Composable
fun PreferenceLink(
    title: String,
    summary: String,
    navController: NavController,
    route: NavigationRoute
) {
    UICard(title = title, description = summary) {
        navController.navigate(route.name)
    }
}