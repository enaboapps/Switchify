package com.enaboapps.switchify.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.rememberNavController
import com.enaboapps.switchify.activities.ui.theme.SwitchifyTheme
import com.enaboapps.switchify.nav.NavGraph
import com.enaboapps.switchify.preferences.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    // Register the ActivityResultLauncher for the update flow
    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            // Handle the update failure or user cancellation
            Log.e("MainActivity", "Update flow failed! Result code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            SwitchifyTheme {
                NavGraph(navController = navController)
            }
        }

        // Initialize PreferenceManager and retrieve settings
        val preferenceManager = PreferenceManager(this)
        preferenceManager.preferenceSync.retrieveSettingsFromFirestore()
        preferenceManager.preferenceSync.listenForSettingsChangesOnRemote()

        // Initialize AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)

        // Check for updates
        checkForUpdates()
    }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // Request the update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
                Log.d("MainActivity", "Update available. Requesting update.")
            }
        }.addOnFailureListener { exception ->
            // Handle the exception (e.g., log it or notify the user)
            Log.e("MainActivity", "Failed to check for updates: ${exception.message}")
        }
    }
}