package com.enaboapps.switchify.service.selection

import android.content.Context
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.gestures.visuals.AutoTapVisual
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.ScanSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This class handles the selection process.
 */
object SelectionHandler {
    private var selectAction: (() -> Unit)? = null
    private var startScanningAction: (() -> Unit)? = null
    private var methodTypeInvokedForStartScanningAction: String? = null
    private var autoSelectInProgress = false
    private var bypassAutoSelect = false
    private var autoTapVisual: AutoTapVisual? = null

    private lateinit var scanSettings: ScanSettings

    /**
     * Initializes the selection handler.
     * This method is intended to be called once, ideally during application startup.
     * It uses the application context to avoid memory leaks.
     *
     * @param appContext The application context used for initialization.
     */
    fun init(appContext: Context) {
        // Use application context to avoid leaking activity or other contexts
        scanSettings = ScanSettings(appContext)
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
     * Sets the start scanning action to be executed.
     *
     * @param newAction The action to be executed as part of the selection process.
     */
    fun setStartScanningAction(newAction: () -> Unit) {
        startScanningAction = newAction
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
        // Check if a linear gesture is in progress
        if (GestureManager.getInstance().isPerformingLinearGesture()) {
            GestureManager.getInstance().endLinearGesture()
            return
        }

        methodTypeInvokedForStartScanningAction = ScanMethod.getType()
        MenuManager.getInstance().scanMethodToRevertTo = ScanMethod.getType()

        // If bypass auto-select is enabled, perform the selection action and return
        if (bypassAutoSelect) {
            selectAction?.invoke()
            performStartScanningAction()
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
        val autoSelectEnabled = scanSettings.isAutoSelectEnabled()
        // If auto-select is enabled, start the auto-select process
        if (autoSelectEnabled) {
            if (!autoSelectInProgress && selectAction != null) {
                autoSelectInProgress = true
                CoroutineScope(Dispatchers.Main).launch {
                    val delayTime = scanSettings.getAutoSelectDelay()
                    val point = GesturePoint.getPoint()
                    autoTapVisual?.start(point.x, point.y, delayTime)
                    delay(delayTime)
                    if (autoSelectInProgress) {
                        selectAction?.invoke()
                        performStartScanningAction()
                        autoSelectInProgress = false
                    }
                }
            }
        } else { // If auto-select is disabled, open the main menu
            MenuManager.getInstance().openMainMenu()
        }
    }

    /**
     * Performs the start scanning action if it is enabled.
     */
    fun performStartScanningAction() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(300)
            if (scanSettings.getAutomaticallyStartScanAfterSelection()) {
                if (methodTypeInvokedForStartScanningAction == ScanMethod.getType()) {
                    startScanningAction?.invoke()
                }
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