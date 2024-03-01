package com.enaboapps.switchify.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
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
    val authManager =
        AuthManager.instance // Assuming AuthManager is already defined in your project
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "Not Logged In"
    val verticalScrollState = rememberScrollState()

    Scaffold(topBar = {
        NavBar(
            title = "Account",
            navController = navController
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(verticalScrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmailAddressView(email = userEmail)
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

/**
 * This composable represents the email address view
 * @param email The email address
 */
@Composable
private fun EmailAddressView(email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Email address",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(8.dp)
            )
            Row {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    modifier = Modifier.padding(8.dp)
                )
                Text(text = email, modifier = Modifier.padding(8.dp))
            }
        }
    }
}