package com.enaboapps.switchify.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.service.utils.ServiceUtils
import com.enaboapps.switchify.widgets.UICard

@Composable
fun HomeScreen(navController: NavController, serviceUtils: ServiceUtils = ServiceUtils()) {
    val context = LocalContext.current
    val isAccessibilityServiceEnabled = serviceUtils.isAccessibilityServiceEnabled(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Switchify", color = Color.White)
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Welcome to Switchify", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                UICard(
                    title = "Settings",
                    description = "Tap here to adjust your settings.",
                    onClick = { navController.navigate(NavigationRoute.Settings.name) }
                )
            }

            if (!isAccessibilityServiceEnabled) {
                item {
                    UICard(
                        title = "Accessibility Service",
                        description = "Tap here to enable the accessibility service.",
                        onClick = { navController.navigate(NavigationRoute.EnableAccessibilityService.name) }
                    )
                }
            }
        }
    }
}