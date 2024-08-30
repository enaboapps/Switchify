package com.enaboapps.switchify.service.selection

import android.content.Context
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.gestures.visuals.AutoTapVisual
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanMethod
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
    private var bypassAutoSelect = false
    private var preferenceManager: PreferenceManager? = null
    private var autoTapVisual: AutoTapVisual? = null

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
        autoTapVisual = AutoTapVisual(appContext.applicationContext)
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
     * Sets the bypass auto-select flag.
     *
     * @param bypass True to bypass auto-select
     */
    fun setBypassAutoSelect(bypass: Boolean) {
        bypassAutoSelect = bypass
    }

    /**
     * Performs the selection action based on the current settings and state.
     */
    fun performSelectionAction() {
        MenuManager.getInstance().scanMethodToRevertTo = ScanMethod.getType()

        // If bypass auto-select is enabled, perform the selection action and return
        if (bypassAutoSelect) {
            selectAction?.invoke()
            return
        }

        // If auto-select is in progress, cancel it and open the main menu
        if (autoSelectInProgress) {
            MenuManager.getInstance().openMainMenu()
            autoTapVisual?.stop()
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
                        val point = GesturePoint.getPoint()
                        autoTapVisual?.start(point.x, point.y, delayTime)
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