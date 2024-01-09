package com.enaboapps.switchify.service.scanning

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore
import java.util.Timer
import java.util.TimerTask

class SwitchListener(
    private val context: Context,
    private val scanningManager: ScanningManager
) {

    private val preferenceManager = PreferenceManager(context)

    private val switchEventStore = SwitchEventStore(context)

    // Variable to track the latest action
    private var latestAction: AbsorbedSwitchAction? = null

    // Timer to time the switch hold time
    private var switchHoldTimer: Timer? = null

    // Function to start the timer
    private fun startSwitchHoldTimer() {
        switchHoldTimer = Timer()
        switchHoldTimer?.schedule(object : TimerTask() {
            override fun run() {
                // If the timer is not null, cancel it
                switchHoldTimer?.cancel()
                switchHoldTimer = null
                // If the latest action is not null, call the long press action on the scanning manager
                if (latestAction != null) {
                    scanningManager.performAction(latestAction!!.switchEvent.longPressAction)
                }
            }
        }, preferenceManager.getIntegerValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME).toLong())
    }

    // This function is called when the switch is pressed
    // It takes in the key code, checks if it is a switch event, and if it is, it updates the latest action
    // If there is no long press action, it calls the press action on the scanning manager
    fun onSwitchPressed(keyCode: Int): Boolean {
        val switchEvent = switchEventStore.find(keyCode.toString())
        Log.d("SwitchListener", "onSwitchPressed: $keyCode")
        if (switchEvent != null) {
            latestAction = AbsorbedSwitchAction(switchEvent, System.currentTimeMillis())

            // If there is no long press action, it calls the press action on the scanning manager
            if (switchEvent.longPressAction.id == SwitchAction.Actions.ACTION_NONE) {
                scanningManager.performAction(switchEvent.pressAction)
            } else {
                // If there is a long press action, it starts the switch hold timer
                startSwitchHoldTimer()
                // If the long press is "Change Scanning Direction", pause scanning
                if (switchEvent.longPressAction.id == SwitchAction.Actions.ACTION_CHANGE_SCANNING_DIRECTION) {
                    scanningManager.pauseScanning()
                }
            }
        } else {
            Log.d("SwitchListener", "No switch event found for key code $keyCode")
        }
        return switchEvent == null
    }

    // This function is called when the switch is released
    // It takes in the key code, checks if it is a switch event, and if it is, it checks if the latest action is the same as the switch event
    // If it is, it checks if there is a long press action, and if there is, it checks if the time between the press and release is greater than the switch hold time
    // If it is, it calls the long press action on the scanning manager
    // If it isn't, it calls the press action on the scanning manager
    fun onSwitchReleased(keyCode: Int): Boolean {
        val switchEvent = switchEventStore.find(keyCode.toString())
        Log.d("SwitchListener", "onSwitchReleased: $keyCode")
        if (switchEvent != null) {
            if (latestAction?.switchEvent == switchEvent) {
                if (switchEvent.longPressAction.id != SwitchAction.Actions.ACTION_NONE) {
                    // If the long press is "Change Scanning Direction", resume scanning
                    if (switchEvent.longPressAction.id == SwitchAction.Actions.ACTION_CHANGE_SCANNING_DIRECTION) {
                        scanningManager.resumeScanning()
                    }

                    val time = System.currentTimeMillis() - latestAction!!.time
                    val switchHoldTime = preferenceManager.getIntegerValue(PreferenceManager.PREFERENCE_KEY_SWITCH_HOLD_TIME)
                    if (time < switchHoldTime) {
                        scanningManager.performAction(switchEvent.pressAction)
                    }
                    // If the timer is not null, cancel it
                    switchHoldTimer?.cancel()
                }
            } else {
                Log.d("SwitchListener", "Switch event does not match latest action")
            }
        }
        return switchEvent == null
    }

}

// This class represents a switch action heard by the switch listener
// It contains the switch event and the time it was heard
data class AbsorbedSwitchAction(val switchEvent: SwitchEvent, val time: Long)