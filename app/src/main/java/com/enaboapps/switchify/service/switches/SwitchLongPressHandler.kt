package com.enaboapps.switchify.service.switches

import android.content.Context
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.switches.SwitchAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This object manages the long press action on a switch.
 */
object SwitchLongPressHandler {
    private var longPressJob: Job? = null
    private var longPressAction: SwitchAction? = null

    /**
     * Initiates the long press action.
     * @param context The context.
     * @param action The action to perform on long press.
     * @param scanningManager The manager responsible for scanning actions.
     */
    fun startLongPress(context: Context, action: SwitchAction, scanningManager: ScanningManager) {
        longPressAction = action
        val holdTime =
            PreferenceManager(context).getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)
        longPressJob = CoroutineScope(Dispatchers.Main).launch {
            delay(holdTime)

            // Toggle swipe lock if enabled 
            if (GestureManager.getInstance().isSwipeLockEnabled()) {
                GestureManager.getInstance().toggleSwipeLock()
                return@launch
            }

            longPressAction?.let {
                scanningManager.performAction(it)
            }
        }
    }

    /**
     * Cancels the ongoing long press action.
     */
    fun stopLongPress() {
        longPressJob?.cancel()
        longPressJob = null
    }
}