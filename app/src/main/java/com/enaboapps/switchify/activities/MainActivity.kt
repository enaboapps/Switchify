package com.enaboapps.switchify.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.enaboapps.switchify.activities.ui.theme.SwitchifyTheme
import com.enaboapps.switchify.nav.NavGraph
import com.enaboapps.switchify.preferences.PreferenceManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        private const val TAG = "MainActivity"
    }

    // Install state listener for tracking update status
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                // Show the snackbar to complete the update
                Toast.makeText(
                    this,
                    "Update downloaded. Restart to install.",
                    Toast.LENGTH_LONG
                ).show()
            }

            InstallStatus.FAILED -> {
                Log.e(TAG, "Update failed! State: ${state.installErrorCode()}")
            }

            else -> {
                Log.d(TAG, "Install Status: ${state.installStatus()}")
            }
        }
    }

    // Register the ActivityResultLauncher for the update flow
    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "Update flow started successfully")
                Toast.makeText(
                    this,
                    "Downloading update...",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Activity.RESULT_CANCELED -> {
                Log.d(TAG, "Update cancelled by user")
            }

            else -> {
                Log.e(TAG, "Update flow failed! Result code: ${result.resultCode}")
                Toast.makeText(
                    this,
                    "Update failed to start",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize managers
        initializeManagers()

        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            SwitchifyTheme {
                SnackbarHost(hostState = snackbarHostState)
                NavGraph(navController = navController)

                // Handle update completion
                LaunchedEffect(Unit) {
                    appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                        if (info.installStatus() == InstallStatus.DOWNLOADED) {
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "An update has been downloaded",
                                    actionLabel = "RESTART",
                                    duration = SnackbarDuration.Indefinite
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    appUpdateManager.completeUpdate()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Check for updates
        checkForUpdates()
    }

    private fun initializeManagers() {
        // Initialize PreferenceManager
        preferenceManager = PreferenceManager(this)
        preferenceManager.preferenceSync.apply {
            retrieveSettingsFromFirestore()
            listenForSettingsChangesOnRemote()
        }

        // Initialize AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    private fun checkForUpdates() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    try {
                        // Request the update
                        val updateOptions =
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateResultLauncher,
                            updateOptions
                        )
                        Log.d(TAG, "Update available. Requesting update.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting update flow", e)
                        Toast.makeText(
                            this,
                            "Failed to start update process",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d(TAG, "No update available or update type not allowed")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to check for updates", exception)
                Toast.makeText(
                    this,
                    "Failed to check for updates",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        // Check if an update has been downloaded but not installed
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                Toast.makeText(
                    this,
                    "Update downloaded. Restart to install.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the listener
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}