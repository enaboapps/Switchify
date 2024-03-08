package com.enaboapps.switchify.service.selection

import android.content.Context
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This class handles the auto-selection process.
 */
object AutoSelectionHandler {
    private var selectAction: (() -> Unit)? = null
    private var autoSelectInProgress = false
    private var preferenceManager: PreferenceManager? = null

    /**
     * Initializes the auto-selection handler.
     * This method is intended to be called once, ideally during application startup.
     * It uses the application context to avoid memory leaks.
     *
     * @param appContext The application context used for initialization.
     */
    fun init(appContext: Context) {
        // Use application context to avoid leaking activity or other contexts
        preferenceManager = PreferenceManager(appContext.applicationContext)
    }

    /**
     * Sets the selection action to be executed.
     *
     * @param newAction The action to be executed as part of the selection process.
     */
    fun setSelectAction(newAction: () -> Unit) {
        selectAction = newAction
    }

    /**
     * Performs the selection action based on the current settings and state.
     */
    fun performSelectionAction() {
        // Set the scan receiver to go back to after the menu is closed
        MenuManager.getInstance().scanReceiverState = ScanReceiver.getState()

        // If auto-select is in progress, cancel it and open the main menu
        if (autoSelectInProgress) {
            MenuManager.getInstance().openMainMenu()
            autoSelectInProgress = false
            return
        }

        println("SelectionHandler.performSelectionAction()")

        // Check if auto-select is enabled
        preferenceManager?.let { prefs ->
            val autoSelectEnabled =
                prefs.getBooleanValue(PreferenceManager.PREFERENCE_KEY_AUTO_SELECT)
            // If auto-select is enabled, start the auto-select process
            if (autoSelectEnabled) {
                if (!autoSelectInProgress && selectAction != null) {
                    autoSelectInProgress = true
                    CoroutineScope(Dispatchers.Main).launch {
                        val delayTime =
                            prefs.getLongValue(PreferenceManager.PREFERENCE_KEY_AUTO_SELECT_DELAY)
                        delay(delayTime)
                        if (autoSelectInProgress) {
                            selectAction?.invoke()
                            autoSelectInProgress = false
                        }
                    }
                }
            } else { // If auto-select is disabled, open the main menu
                MenuManager.getInstance().openMainMenu()
            }
        }
    }

    /**
     * Checks if the auto-select process is currently in progress.
     *
     * @return True if the auto-select process is in progress, false otherwise.
     */
    fun isAutoSelectInProgress(): Boolean = autoSelectInProgress
}