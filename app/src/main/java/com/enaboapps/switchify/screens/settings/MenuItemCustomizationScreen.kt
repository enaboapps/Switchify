package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.menu.store.MenuItemStore
import com.enaboapps.switchify.widgets.NavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCustomizationScreen(navController: NavController) {
    val context = LocalContext.current
    val preferenceManager = PreferenceManager(context)
    val menuItemStore = MenuItemStore(context)

    Scaffold(
        topBar = {
            NavBar(title = "Customize Menu Items", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            menuItemStore.mainMenuObject.getMenuItems().forEach { menuItem ->
                val isVisible = remember { mutableStateOf(preferenceManager.getMenuItemVisibility(menuItem.id)) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = menuItem.text)
                    Switch(
                        checked = isVisible.value,
                        onCheckedChange = {
                            isVisible.value = it
                            preferenceManager.setMenuItemVisibility(menuItem.id, it)
                            menuItem.visible = it
                        }
                    )
                }
            }
        }
    }
}
