package com.enaboapps.switchify.screens.settings.actions

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
import com.enaboapps.switchify.service.custom.actions.store.ActionStore
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_COPY_TEXT_TO_CLIPBOARD
import com.enaboapps.switchify.service.custom.actions.store.data.ACTION_OPEN_APP
import com.enaboapps.switchify.service.custom.actions.store.data.ActionExtra
import com.enaboapps.switchify.utils.AppLauncher
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.Picker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActionScreen(navController: NavController, actionId: String? = null) {
    val context = LocalContext.current
    val actionStore = remember { ActionStore(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditMode = actionId != null
    val screenTitle = if (isEditMode) "Edit Action" else "Add Action"

    val availableActions = remember { actionStore.getAvailableActions() }

    var selectedAction by remember { mutableStateOf(availableActions.first()) }
    var selectedExtra by remember { mutableStateOf<ActionExtra?>(null) }
    var actionText by remember { mutableStateOf("") }
    var extraValid by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing action data if in edit mode
    LaunchedEffect(actionId) {
        if (isEditMode) {
            actionStore.getAction(actionId)?.let { action ->
                actionText = action.text
                selectedAction = action.action
                selectedExtra = action.extra
                println("Loaded action: $action")
            }
        }
    }

    val saveButtonEnabled = remember(actionText, selectedAction, selectedExtra, extraValid) {
        actionText.isNotBlank() && selectedAction.isNotBlank() && selectedExtra != null && extraValid
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
            ActionTextInput(
                text = actionText,
                onTextChange = { actionText = it }
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
                        actionStore.updateAction(
                            id = actionId,
                            action = selectedAction,
                            text = actionText,
                            extra = selectedExtra
                        )
                    } else {
                        actionStore.addAction(
                            action = selectedAction,
                            text = actionText,
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
private fun ActionTextInput(
    text: String,
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("Action Text") },
        modifier = Modifier.fillMaxWidth(),
        isError = text.isBlank(),
        supportingText = {
            if (text.isBlank()) {
                Text("Action text is required")
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

        ACTION_COPY_TEXT_TO_CLIPBOARD -> CopyTextExtraInput(
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
private fun CopyTextExtraInput(
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = selectedExtra?.textToCopy ?: "",
        onValueChange = { text ->
            onExtraUpdated(
                ActionExtra(
                    textToCopy = text
                )
            )

            onExtraValidated(text.isNotBlank())
        },
        label = { Text("Text to Copy") },
        modifier = Modifier.fillMaxWidth(),
        isError = selectedExtra?.textToCopy.isNullOrBlank() == true,
        supportingText = {
            if (selectedExtra?.textToCopy.isNullOrBlank() == true) {
                Text("Text to copy is required")
            }
        }
    )
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
        text = if (isEditMode) "Update Action" else "Add Action",
        enabled = isEnabled && !isSaving,
        onClick = onSaveClicked
    )
}