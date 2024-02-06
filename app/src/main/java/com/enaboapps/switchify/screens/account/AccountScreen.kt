package com.enaboapps.switchify.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountScreen(navController: NavController) {
    val authManager = AuthManager.instance // Assuming AuthManager is already defined in your project
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "Not Logged In"
    val verticalScrollState = rememberScrollState()

    Scaffold(topBar = { NavBar(title = "Account", navController = navController) }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(verticalScrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Email: $userEmail", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(20.dp))

            FullWidthButton(
                text = "Change Password",
                onClick = {
                    navController.navigate(NavigationRoute.ChangePassword.name)
                }
            )

            FullWidthButton(
                text = "Sign Out",
                onClick = {
                    authManager.signOut()
                    navController.popBackStack(navController.graph.startDestinationId, false)
                }
            )
        }
    }
}