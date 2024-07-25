package com.enaboapps.switchify.widgets

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun NavBar(title: String, navController: NavController) {
    val canGoBack = navController.previousBackStackEntry != null
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = if (canGoBack) {
            {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        } else {
            null
        }
    )
}