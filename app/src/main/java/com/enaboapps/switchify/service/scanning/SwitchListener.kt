package com.enaboapps.switchify.service.scanning

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class SwitchListener(
    private val context: Context,
    private val scanningManager: ScanningManager
) {

    private val switchEventStore = SwitchEventStore(context)

    // Variable to track the latest action
    private var latestAction: SwitchAction? = null

    // This function is called when the switch is pressed
    // It takes in the key code, checks if it is a switch event, and if it is, it updates the latest action
    fun onSwitchPressed(keyCode: Int) {
        val switchEvent = switchEventStore.find(keyCode.toString())
        Log.d("SwitchListener", "onSwitchPressed: $keyCode")
        if (switchEvent != null) {
            latestAction = SwitchAction(switchEvent, System.currentTimeMillis())
        } else {
            Log.d("SwitchListener", "No switch event found for key code $keyCode")
        }
    }

    // This function is called when the switch is released
    // It takes in the key code, checks if it is a switch event, and if it is, it checks if the latest action is the same as the switch event
    // If it is, it calls the select function on the scanning manager
    fun onSwitchReleased(keyCode: Int) {
        val switchEvent = switchEventStore.find(keyCode.toString())
        Log.d("SwitchListener", "onSwitchReleased: $keyCode")
        if (switchEvent != null) {
            if (latestAction?.switchEvent == switchEvent) {
                scanningManager.select()
            }
        } else {
            Toast.makeText(context, "No switch event found for key code $keyCode", Toast.LENGTH_SHORT).show()
        }
    }

}

// This class represents a switch action heard by the switch listener
// It contains the switch event and the time it was heard
data class SwitchAction(val switchEvent: SwitchEvent, val time: Long)