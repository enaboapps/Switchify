package com.enaboapps.switchify.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.service.utils.ServiceUtils

@Composable
fun HomeScreen(navController: NavController, serviceUtils: ServiceUtils = ServiceUtils()) {
    val context = LocalContext.current

    // LaunchedEffect for conditional navigation
    LaunchedEffect(Unit) {
        if (!serviceUtils.isAccessibilityServiceEnabled(context.applicationContext as Application)) {
            navController.navigate(NavigationRoute.EnableAccessibilityService.name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Switchify")
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavigationRoute.Settings.name)
                    }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            Text(text = "Home")
            // Additional UI components can be added here
        }
    }
}