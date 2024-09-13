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
 * This object manages the long press actions on a switch.
 */
object SwitchLongPressHandler {
    private var longPressJob: Job? = null
    private var holdActions: List<SwitchAction>? = null

    /**
     * Initiates the long press action sequence.
     * @param context The context.
     * @param actions The list of actions to perform on long press.
     * @param scanningManager The manager responsible for scanning actions.
     */
    fun startLongPress(
        context: Context,
        actions: List<SwitchAction>,
        scanningManager: ScanningManager
    ) {
        holdActions = actions
        val holdTime = PreferenceManager(context)
            .getLongValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)

        longPressJob = CoroutineScope(Dispatchers.Main).launch {
            delay(holdTime)

            // Toggle gesture lock if enabled
            if (GestureManager.getInstance().isGestureLockEnabled()) {
                GestureManager.getInstance().toggleGestureLock()
                return@launch
            }

            holdActions?.let { actionsList ->
                for (action in actionsList) {
                    scanningManager.performAction(action)
                    delay(holdTime) // Use the switch hold time as delay between actions
                }
            }
        }
    }

    /**
     * Cancels the ongoing long press action sequence.
     */
    fun stopLongPress() {
        longPressJob?.cancel()
        longPressJob = null
    }
}