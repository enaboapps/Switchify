package com.enaboapps.switchify.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.enaboapps.switchify.activities.ui.theme.SwitchifyTheme
import com.enaboapps.switchify.nav.NavGraph
import com.enaboapps.switchify.preferences.PreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            SwitchifyTheme {
                NavGraph(navController = navController)
            }
        }

        val preferenceManager = PreferenceManager(this)
        preferenceManager.preferenceSync.retrieveSettingsFromFirestore()
        preferenceManager.preferenceSync.listenForSettingsChangesOnRemote()
    }
}