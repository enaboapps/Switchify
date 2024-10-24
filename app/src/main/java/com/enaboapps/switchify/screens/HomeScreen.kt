package com.enaboapps.switchify.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.auth.AuthManager
import com.enaboapps.switchify.keyboard.utils.KeyboardUtils
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.utils.ServiceUtils
import com.enaboapps.switchify.switches.SwitchConfigInvalidBanner
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.NavBarAction
import com.enaboapps.switchify.widgets.NavRouteLink

@Composable
fun HomeScreen(navController: NavController, serviceUtils: ServiceUtils = ServiceUtils()) {
    val context = LocalContext.current
    val isAccessibilityServiceEnabled = serviceUtils.isAccessibilityServiceEnabled(context)
    val isSwitchifyKeyboardEnabled = KeyboardUtils.isSwitchifyKeyboardEnabled(context)
    val isSetupComplete = PreferenceManager(context).isSetupComplete()

    val feedbackNavButton = NavBarAction(
        text = "Feedback",
        onClick = {
            val url = "https://switchify.featurebase.app/"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    )

    LaunchedEffect(isSetupComplete) {
        if (!isSetupComplete) {
            navController.navigate(NavigationRoute.Setup.name)
        }
    }

    Scaffold(
        topBar = {
            NavBar(
                title = "Switchify",
                navController = navController,
                actions = listOf(feedbackNavButton)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Welcome to Switchify",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                NavRouteLink(
                    title = "Settings",
                    summary = "Tap here to adjust your settings.",
                    navController = navController,
                    route = NavigationRoute.Settings.name
                )
            }

            item {
                SwitchConfigInvalidBanner(SwitchEventStore(LocalContext.current).isConfigInvalid())
            }

            if (!isAccessibilityServiceEnabled) {
                item {
                    NavRouteLink(
                        title = "Accessibility Service",
                        summary = "Tap here to enable the accessibility service.",
                        navController = navController,
                        route = NavigationRoute.EnableAccessibilityService.name
                    )
                }
            }

            if (!isSwitchifyKeyboardEnabled) {
                item {
                    NavRouteLink(
                        title = "Switchify Keyboard",
                        summary = "Tap here to enable the Switchify keyboard.",
                        navController = navController,
                        route = NavigationRoute.EnableSwitchifyKeyboard.name
                    )
                }
            }

            item {
                NavRouteLink(
                    title = "How To Use",
                    summary = "Learn how to use Switchify.",
                    navController = navController,
                    route = NavigationRoute.HowToUse.name
                )
            }

            item {
                AccountCard(navController)
            }
        }
    }
}


/**
 * Account card
 * If the user is logged in, show the account email and on click, go to the account screen
 * If the user is not logged in, on click, launch the sign in intent
 */
@Composable
fun AccountCard(navController: NavController) {
    val authManager = AuthManager.instance

    val isUserSignedIn = authManager.isUserSignedIn()
    val currentUser = authManager.getCurrentUser()

    val title = if (isUserSignedIn) "Account" else "Sign In"
    val description =
        if (isUserSignedIn) currentUser?.email ?: "" else "Sign in to access your settings."

    NavRouteLink(
        title = title,
        summary = description,
        navController = navController,
        route = if (isUserSignedIn) NavigationRoute.Account.name else NavigationRoute.SignIn.name
    )
}