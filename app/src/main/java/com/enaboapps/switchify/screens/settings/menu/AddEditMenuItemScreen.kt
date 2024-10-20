package com.enaboapps.switchify.screens.settings.menu

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMenuItemScreen(navController: NavController, menuItemId: String? = null) {
    val context = LocalContext.current
    val menuItemJsonStore = remember { MenuItemJsonStore(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = menuItemId != null
    val screenTitle = if (isEditMode) "Edit Action" else "Add Action"

    val availableActions = remember { menuItemJsonStore.getAvailableActions() }

    var selectedAction by remember { mutableStateOf(availableActions.first()) }
    var selectedExtra by remember { mutableStateOf<ActionExtra?>(null) }
    var menuItemText by remember { mutableStateOf("") }
    var extraValid by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing menu item data if in edit mode
    LaunchedEffect(menuItemId) {
        if (isEditMode) {
            menuItemJsonStore.getMenuItem(menuItemId)?.let { menuItem ->
                menuItemText = menuItem.text
                selectedAction = menuItem.action
                selectedExtra = menuItem.extra
                println("Loaded menu item: $menuItem")
            }
        }
    }

    val saveButtonEnabled = remember(menuItemText, selectedAction, selectedExtra, extraValid) {
        menuItemText.isNotBlank() && selectedAction.isNotBlank() && selectedExtra != null && extraValid
    }

    Scaffold(
        topBar = {
            NavBar(title = screenTitle, navController = navController)
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
            MenuItemTextInput(
                text = menuItemText,
                onTextChange = { menuItemText = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionPicker(
                selectedAction = selectedAction,
                availableActions = availableActions,
                onActionSelected = {
                    selectedAction = it
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionExtraInput(
                selectedAction = selectedAction,
                selectedExtra = selectedExtra,
                onExtraUpdated = { selectedExtra = it },
                onExtraValidated = { extraValid = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SaveButton(
                isEditMode = isEditMode,
                isSaving = isSaving,
                isEnabled = saveButtonEnabled,
                onSaveClicked = {
                    isSaving = true
                    if (isEditMode) {
                        menuItemJsonStore.updateMenuItem(
                            id = menuItemId,
                            action = selectedAction,
                            text = menuItemText,
                            extra = selectedExtra
                        )
                    } else {
                        menuItemJsonStore.addMenuItem(
                            action = selectedAction,
                            text = menuItemText,
                            extra = selectedExtra
                        )
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun MenuItemTextInput(
    text: String,
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("Menu Item Text") },
        modifier = Modifier.fillMaxWidth(),
        isError = text.isBlank(),
        supportingText = {
            if (text.isBlank()) {
                Text("Menu item text is required")
            }
        }
    )
}

@Composable
private fun ActionPicker(
    selectedAction: String,
    availableActions: List<String>,
    onActionSelected: (String) -> Unit
) {
    Picker(
        title = "Select Action",
        selectedItem = selectedAction,
        items = availableActions,
        onItemSelected = onActionSelected,
        itemToString = { it },
        itemDescription = { it }
    )
}

@Composable
private fun ActionExtraInput(
    selectedAction: String,
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    when (selectedAction) {
        ACTION_OPEN_APP -> AppLaunchExtraInput(
            selectedExtra = selectedExtra,
            onExtraUpdated = onExtraUpdated,
            onExtraValidated = onExtraValidated
        )
        // Add more cases here for future action types
        else -> {
            // Handle unknown action types or actions without extras
            onExtraUpdated(null)
            onExtraValidated(true)
        }
    }
}

@Composable
private fun AppLaunchExtraInput(
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    AppLaunchPicker(
        initialApp = selectedExtra?.let {
            AppLauncher.AppInfo(
                it.appName,
                it.appPackage
            )
        },
        onAppSelected = { appInfo ->
            onExtraUpdated(
                ActionExtra(
                    appName = appInfo.displayName,
                    appPackage = appInfo.packageName
                )
            )
            onExtraValidated(true)
        }
    )
}

@Composable
private fun SaveButton(
    isEditMode: Boolean,
    isSaving: Boolean,
    isEnabled: Boolean,
    onSaveClicked: () -> Unit
) {
    FullWidthButton(
        text = if (isEditMode) "Update Menu Item" else "Add Menu Item",
        enabled = isEnabled && !isSaving,
        onClick = onSaveClicked
    )
}