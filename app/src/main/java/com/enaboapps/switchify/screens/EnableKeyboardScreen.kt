package com.enaboapps.switchify.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.keyboard.utils.KeyboardUtils
import com.enaboapps.switchify.widgets.FullWidthButton
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun EnableKeyboardScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            NavBar(title = "Enable Switchify Keyboard", navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "To use Switchify effectively, please enable the Switchify Keyboard in your device settings.",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            FullWidthButton(text = "Take Me There", onClick = {
                KeyboardUtils.openInputMethodSettings(context)
            })
            FullWidthButton(text = "I've Enabled It", onClick = {
                navController.popBackStack()
            })
            FullWidthButton(text = "Not Right Now", onClick = {
                navController.popBackStack()
            })
        }
    }
}