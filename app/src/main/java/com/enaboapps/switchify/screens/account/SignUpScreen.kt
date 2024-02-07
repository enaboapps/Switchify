package com.enaboapps.switchify.screens.account

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val authManager = AuthManager.instance
    val verticalScrollState = rememberScrollState()

    Scaffold(topBar = { NavBar(title = "Sign Up", navController = navController) }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(verticalScrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            // Email TextField
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email // Suggests email input for autofill
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Password TextField
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password // Suggests password input for autofill
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Confirm Password TextField
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password // Consistency for autofill services
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            FullWidthButton(
                text = "Sign Up",
                onClick = {
                    errorMessage = when {
                        !authManager.isPasswordStrong(password) -> "Password must be at least 8 characters long, include an uppercase letter, a lowercase letter, and a number."
                        password != confirmPassword -> "Passwords do not match."
                        else -> null
                    }
                    if (errorMessage == null) {
                        authManager.createUserWithEmailAndPassword(email, password,
                            onSuccess = {
                                // Go to the first screen
                                navController.popBackStack(navController.graph.startDestinationId, false)
                            },
                            onFailure = { exception ->
                                errorMessage = exception.localizedMessage
                            }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val urlLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    // Handle the result
                }
            val privacyPolicyUrl = "https://www.enaboapps.com/switchify-privacy-policy"
            TextButton(onClick = {
                // Open the privacy policy in the system browser
                urlLauncher.launch(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)))
            }) {
                Text("Privacy Policy")
            }
        }
    }
}