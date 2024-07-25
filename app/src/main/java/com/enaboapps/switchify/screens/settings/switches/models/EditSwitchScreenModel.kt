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
    val pressAction = MutableLiveData(SwitchAction(SwitchAction.Actions.ACTION_SELECT))
    val longPressAction = MutableLiveData(SwitchAction(SwitchAction.Actions.ACTION_STOP_SCANNING))


    init {
        val event = store.find(code)
        name.value = event?.name
        pressAction.value = event?.pressAction
        longPressAction.value = event?.longPressAction
    }

    fun save(completion: () -> Unit) {
        val event = SwitchEvent(
            name = name.value!!,
            code = code,
            pressAction = pressAction.value!!,
            longPressAction = longPressAction.value!!
        )
        store.update(event)
        completion()
    }

    fun delete(completion: () -> Unit) {
        val event = store.find(code)
        event?.let {
            store.remove(it)
            completion()
        }
    }

}