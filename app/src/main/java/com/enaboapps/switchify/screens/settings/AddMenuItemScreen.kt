package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.store.MenuItemJsonStore
import com.enaboapps.switchify.widgets.NavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemScreen(navController: NavController) {
    val context = LocalContext.current
    val menuItemJsonStore = MenuItemJsonStore(context)

    var id by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var drawableId by remember { mutableStateOf(0) }
    var drawableDescription by remember { mutableStateOf("") }
    var closeOnSelect by remember { mutableStateOf(true) }
    var isLinkToMenu by remember { mutableStateOf(false) }
    var isMenuHierarchyManipulator by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            NavBar(title = "Add Menu Item", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Text") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = drawableId.toString(),
                onValueChange = { drawableId = it.toIntOrNull() ?: 0 },
                label = { Text("Drawable ID") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = drawableDescription,
                onValueChange = { drawableDescription = it },
                label = { Text("Drawable Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Close on Select")
                Switch(
                    checked = closeOnSelect,
                    onCheckedChange = { closeOnSelect = it }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Is Link to Menu")
                Switch(
                    checked = isLinkToMenu,
                    onCheckedChange = { isLinkToMenu = it }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Is Menu Hierarchy Manipulator")
                Switch(
                    checked = isMenuHierarchyManipulator,
                    onCheckedChange = { isMenuHierarchyManipulator = it }
                )
            }
            OutlinedTextField(
                value = page.toString(),
                onValueChange = { page = it.toIntOrNull() ?: 0 },
                label = { Text("Page") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val newMenuItem = MenuItem(
                        id = id,
                        text = text,
                        drawableId = drawableId,
                        drawableDescription = drawableDescription,
                        closeOnSelect = closeOnSelect,
                        isLinkToMenu = isLinkToMenu,
                        isMenuHierarchyManipulator = isMenuHierarchyManipulator,
                        page = page,
                        action = {}
                    )
                    menuItemJsonStore.addMenuItem(newMenuItem)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
