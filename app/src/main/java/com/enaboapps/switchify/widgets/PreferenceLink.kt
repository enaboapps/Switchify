package com.enaboapps.switchify.widgets

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun PreferenceLink(
    title: String,
    summary: String,
    navController: NavController,
    route: String
) {
    UICard(title = title, description = summary) {
        navController.navigate(route)
    }
}