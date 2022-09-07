package com.enaboapps.switchify.screens.settings.models

import android.util.Log
import androidx.compose.ui.input.key.Key
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class AddNewSwitchScreenModel(private val store: SwitchEventStore): ViewModel() {

    private val TAG = "AddNewSwitchScreenModel"

    var code: Long = 0

    val name = MutableLiveData(
        "Switch ${store.getCount() + 1}"
    )

    val shouldSave = MutableLiveData(false)

    fun processKeyCode(key: Key) {
        Log.d(TAG, "processKeyCode: $key")
        code = key.keyCode
        shouldSave.value = true
    }

    fun save() {
        if (shouldSave.value == true) {
            val event = SwitchEvent(name = name.value!!, code = code.toString(), pressAction = SwitchAction(SwitchAction.Actions.ACTION_SELECT))
            if (!store.contains(event)) {
                store.add(event)
            } else {
                shouldSave.value = false
            }
        }
    }

}