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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun SignInScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val verticalScrollState = rememberScrollState()

    Scaffold(topBar = { NavBar(title = "Sign In", navController = navController) }) { paddingValues ->
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
            // Email TextField with KeyboardOptions for Email
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email // Helps with autofill support
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Password TextField with KeyboardOptions for Password
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password // Helps with autofill support
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            FullWidthButton(
                text = "Sign In",
                onClick = {
                    AuthManager.instance.signInWithEmailAndPassword(email, password,
                        onSuccess = {
                            navController.popBackStack()
                        },
                        onFailure = { exception ->
                            errorMessage = exception.localizedMessage
                        }
                    )
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            FullWidthButton(
                text = "Sign Up",
                onClick = {
                    navController.navigate(NavigationRoute.SignUp.name)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                navController.navigate(NavigationRoute.ForgotPassword.name)
            }) {
                Text("Forgot Password?")
            }
        }
    }
}