package com.enaboapps.switchify.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun NavRouteLink(
    title: String,
    summary: String,
    navController: NavController,
    route: String
) {
    UICard(
        title = title,
        description = summary,
        rightIcon = Icons.AutoMirrored.Filled.ArrowForward
    ) {
        navController.navigate(route)
    }
}