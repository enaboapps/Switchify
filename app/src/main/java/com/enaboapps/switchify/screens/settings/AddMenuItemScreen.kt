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
import com.enaboapps.switchify.service.custom.actions.AppLaunchPicker
import com.enaboapps.switchify.service.custom.actions.data.ACTION_OPEN_APP
import com.enaboapps.switchify.service.custom.actions.data.ActionExtra
import com.enaboapps.switchify.service.menu.store.MenuItemJsonStore
import com.enaboapps.switchify.utils.AppLauncher
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.Picker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemScreen(navController: NavController) {
    val context = LocalContext.current
    val menuItemJsonStore = MenuItemJsonStore(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val availableActions = remember { mutableStateListOf<String>() }
    availableActions.addAll(menuItemJsonStore.getAvailableActions())

    val selectedAction = remember { mutableStateOf(availableActions.first()) }
    val selectedExtra = remember { mutableStateOf<ActionExtra?>(null) }
    val menuItemText = remember { mutableStateOf("") }

    val saveButtonEnabled = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            NavBar(title = "Add Menu Item", navController = navController)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = menuItemText.value,
                onValueChange = { menuItemText.value = it },
                label = { Text("Menu Item Text") },
                modifier = Modifier.fillMaxWidth(),
                isError = menuItemText.value.isBlank(),
                supportingText = {
                    if (menuItemText.value.isBlank()) {
                        Text("Menu item text is required")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Picker(
                title = "Select Action",
                selectedItem = selectedAction.value,
                items = availableActions,
                onItemSelected = { action ->
                    selectedAction.value = action
                },
                itemToString = { it },
                itemDescription = { it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedAction.value) {
                ACTION_OPEN_APP -> AppLaunchPicker(
                    initialApp = selectedExtra.value?.let {
                        AppLauncher.AppInfo(
                            it.appName,
                            it.appPackage
                        )
                    },
                    onAppSelected = { appInfo ->
                        selectedExtra.value = ActionExtra(
                            appName = appInfo.displayName,
                            appPackage = appInfo.packageName
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FullWidthButton(
                text = "Add Menu Item",
                enabled = !saveButtonEnabled.value,
                onClick = {
                    when {
                        menuItemText.value.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Menu item text is required")
                            }
                        }

                        selectedAction.value == ACTION_OPEN_APP && selectedExtra.value == null -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please select an app to launch")
                            }
                        }

                        else -> {
                            saveButtonEnabled.value = false
                            val id = menuItemJsonStore.addMenuItem(
                                action = selectedAction.value,
                                text = menuItemText.value,
                                extra = selectedExtra.value
                            )
                            println("ID: $id")
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}