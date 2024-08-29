package com.enaboapps.switchify.widgets

import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class NavBarAction(
    val text: String,
    val onClick: () -> Unit
)

@Composable
fun NavBar(
    title: String,
    navController: NavController,
    actions: List<NavBarAction> = emptyList()
) {
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
        },
        actions = {
            actions.forEach { action ->
                TextButton(
                    onClick = action.onClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.onPrimary)
                ) {
                    Text(action.text)
                }
            }
        },
        elevation = 4.dp
    )
}