package com.enaboapps.switchify.screens

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute

@Composable
fun HomeScreen(navController: NavController) {
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
    ) {
    }
}
