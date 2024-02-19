package com.enaboapps.switchify.screens.settings.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enaboapps.switchify.preferences.PreferenceManager
import kotlinx.coroutines.launch

class SettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    // Initialize MutableLiveData with initial values from PreferenceManager
    private val _scanRate = MutableLiveData<Long>().apply {
        value = preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }
    val scanRate: LiveData<Long> = _scanRate

    private val _refineScanRate = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
    }
    val refineScanRate: LiveData<Long> = _refineScanRate

    private val _switchHoldTime = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME)
    }
    val switchHoldTime: LiveData<Long> = _switchHoldTime

    private val _autoSelect = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }
    val autoSelect: LiveData<Boolean> = _autoSelect

    private val _autoSelectDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
    }
    val autoSelectDelay: LiveData<Long> = _autoSelectDelay


    private val pauseScanOnSwitchHoldThreshold: Long = 400

    // LiveData for the switch stability setting visibility
    private val _switchStabilityVisible = MutableLiveData<Boolean>()
    val switchStabilityVisible: LiveData<Boolean> = _switchStabilityVisible


    init {
        updateSwitchStabilityVisible()
    }


    // Update methods now update MutableLiveData which in turn updates the UI
    fun setScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
            _scanRate.postValue(rate)
        }
        // If rate < pauseScanOnSwitchHoldThreshold, set pauseScanOnSwitchHold to true
        if (rate < pauseScanOnSwitchHoldThreshold) {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
                true
            )
        }
        updateSwitchStabilityVisible()
    }

    fun setRefineScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE,
                rate
            )
            _refineScanRate.postValue(rate)
        }
        // If rate < pauseScanOnSwitchHoldThreshold, set pauseScanOnSwitchHold to true
        if (rate < pauseScanOnSwitchHoldThreshold) {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
                true
            )
        }
        updateSwitchStabilityVisible()
    }

    fun setAutoSelect(autoSelect: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT,
                autoSelect
            )
            _autoSelect.postValue(autoSelect)
        }
    }

    fun setAutoSelectDelay(delay: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY,
                delay
            )
            _autoSelectDelay.postValue(delay)
        }
    }


    // Update the switchStabilityVisible LiveData when scan rate or refine scan rate changes
    private fun updateSwitchStabilityVisible() {
        val scanRate =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        val refineScanRate =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
        _switchStabilityVisible.postValue(scanRate > pauseScanOnSwitchHoldThreshold && refineScanRate > pauseScanOnSwitchHoldThreshold)
    }
}