package com.enaboapps.switchify.screens.setup

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.utils.ServiceUtils
import com.enaboapps.switchify.switches.SwitchEventStore

class SetupScreenModel(context: Context) : ViewModel() {
    private val switchEventStore = SwitchEventStore(context)
    private val serviceUtils = ServiceUtils()

    val switchCount = MutableLiveData<Int>()
    val isAccessibilityServiceEnabled = MutableLiveData<Boolean>()

    init {
        switchCount.value = switchEventStore.getCount()
        isAccessibilityServiceEnabled.value = serviceUtils.isAccessibilityServiceEnabled(context)
    }

    fun setSetupComplete(context: Context) {
        PreferenceManager(context).setSetupComplete()
    }
}