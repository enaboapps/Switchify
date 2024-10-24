package com.enaboapps.switchify.screens.setup

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.keyboard.utils.KeyboardUtils
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.utils.ServiceUtils
import com.enaboapps.switchify.switches.SwitchEventStore

class SetupScreenModel(context: Context) : ViewModel() {
    private val switchEventStore = SwitchEventStore(context)
    private val serviceUtils = ServiceUtils()

    val switchesInvalid = MutableLiveData<String?>()
    val isAccessibilityServiceEnabled = MutableLiveData<Boolean>()
    val isSwitchifyKeyboardEnabled = MutableLiveData<Boolean>()

    init {
        isAccessibilityServiceEnabled.value = serviceUtils.isAccessibilityServiceEnabled(context)
        isSwitchifyKeyboardEnabled.value = KeyboardUtils.isSwitchifyKeyboardEnabled(context)
        switchesInvalid.value = switchEventStore.isConfigInvalid()
    }

    fun checkSwitches() {
        switchesInvalid.value = switchEventStore.isConfigInvalid()
    }

    fun setSetupComplete(context: Context) {
        PreferenceManager(context).setSetupComplete()
    }
}