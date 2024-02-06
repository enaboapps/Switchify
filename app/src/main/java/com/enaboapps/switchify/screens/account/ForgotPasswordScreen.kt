package com.enaboapps.switchify.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val authManager = AuthManager.instance
    val scrollState = rememberScrollState()

    Scaffold(topBar = {
        NavBar(
            title = "Reset Password",
            navController = navController
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top, // Ensures content starts from the top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (message != null) {
                Text(
                    text = message ?: "",
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            FullWidthButton(
                text = "Send Reset Link",
                onClick = {
                    if (email.isNotBlank()) {
                        authManager.sendPasswordResetEmail(email,
                            onSuccess = {
                                message = "Reset link sent to your email. Please check your inbox."
                            },
                            onFailure = { exception ->
                                message = "Error: ${exception.localizedMessage}. Please try again."
                            }
                        )
                    } else {
                        message = "Please enter your email address."
                    }
                }
            )
        }
    }
}