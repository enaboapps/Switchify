package com.enaboapps.switchify.screens.settings.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.switches.SwitchEventStore

class SwitchesScreenModel(private val store: SwitchEventStore) : ViewModel() {

    val events = MutableLiveData(
        store.getSwitchEvents()
    )

    fun loadEvents() {
        events.postValue(store.getSwitchEvents())
    }

}