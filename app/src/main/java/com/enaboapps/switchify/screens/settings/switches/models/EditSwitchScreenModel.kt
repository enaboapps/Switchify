package com.enaboapps.switchify.screens.settings.switches.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore

class EditSwitchScreenModel(
    private val code: String,
    private val store: SwitchEventStore
) : ViewModel() {

    val name = MutableLiveData("")
    val pressAction = MutableLiveData(SwitchAction(SwitchAction.ACTION_SELECT))
    val longPressActions = MutableLiveData<List<SwitchAction>>(emptyList())

    init {
        val event = store.find(code)
        name.value = event?.name
        pressAction.value = event?.pressAction
        longPressActions.value =
            event?.holdActions ?: listOf() // Initialize with multiple long press actions
    }

    fun save(completion: () -> Unit) {
        val event = SwitchEvent(
            name = name.value!!,
            code = code,
            pressAction = pressAction.value!!,
            holdActions = longPressActions.value!! // Save the list of long press actions
        )
        store.update(event)
        completion()
    }

    fun updateLongPressAction(oldAction: SwitchAction, newAction: SwitchAction) {
        val currentActions = longPressActions.value?.toMutableList() ?: mutableListOf()
        val index = currentActions.indexOf(oldAction)
        if (index != -1) {
            currentActions[index] = newAction
            longPressActions.value = currentActions
        }
    }

    fun delete(completion: () -> Unit) {
        val event = store.find(code)
        event?.let {
            store.remove(it)
            completion()
        }
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
}