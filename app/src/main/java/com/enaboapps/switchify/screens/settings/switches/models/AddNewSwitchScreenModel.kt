package com.enaboapps.switchify.screens.settings.switches.models

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class AddNewSwitchScreenModel(private val store: SwitchEventStore) : ViewModel() {

    private val TAG = "AddNewSwitchScreenModel"

    private var code: Int = 0

    val name = MutableLiveData(
        "Switch ${store.getCount() + 1}"
    )

    val shouldSave = MutableLiveData(false)

    // Actions for press and long press
    val pressAction = MutableLiveData(SwitchAction(SwitchAction.Companion.Actions.ACTION_SELECT))
    val longPressActions = MutableLiveData<List<SwitchAction>>(emptyList())

    fun processKeyCode(key: Key, context: Context) {
        Log.d(TAG, "processKeyCode: ${key.nativeKeyCode}")

        // If switch already exists, don't save and show toast
        if (store.find(key.nativeKeyCode.toString()) != null) {
            shouldSave.value = false
            Toast.makeText(context, "Switch already exists", Toast.LENGTH_SHORT).show()
            return
        }

        code = key.nativeKeyCode
        shouldSave.value = true
    }

    fun addLongPressAction(action: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        currentActions.add(action)
        longPressActions.value = currentActions
    }

    fun removeLongPressAction(action: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        currentActions.remove(action)
        longPressActions.value = currentActions
    }

    fun updateLongPressAction(oldAction: SwitchAction, newAction: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        val index = currentActions.indexOf(oldAction)
        if (index != -1) {
            currentActions[index] = newAction
            longPressActions.value = currentActions
        }
    }

    fun save() {
        if (shouldSave.value == true) {
            val event = SwitchEvent(
                name = name.value!!,
                code = code.toString(),
                pressAction = pressAction.value!!,
                holdActions = longPressActions.value!!
            )
            if (store.find(event.code) == null) {
                store.add(event)
            } else {
                shouldSave.value = false
            }
        }
    }
}