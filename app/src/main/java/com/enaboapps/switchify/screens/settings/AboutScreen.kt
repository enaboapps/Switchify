package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val version = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            NavBar(title = "About", navController = navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(it)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = context.getString(com.enaboapps.switchify.R.string.app_name),
                style = MaterialTheme.typography.h4
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "Version $version",
                style = MaterialTheme.typography.body1
            )
        }
    }
}