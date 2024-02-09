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

    private val _pauseScanOnSwitchHold = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD)
    }
    val pauseScanOnSwitchHold: LiveData<Boolean> = _pauseScanOnSwitchHold

    private val _autoSelect = MutableLiveData<Boolean>().apply {
        value = preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }
    val autoSelect: LiveData<Boolean> = _autoSelect

    private val _autoSelectDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
    }
    val autoSelectDelay: LiveData<Long> = _autoSelectDelay

    // Update methods now update MutableLiveData which in turn updates the UI
    fun setScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
            _scanRate.postValue(rate)
        }
    }

    fun setRefineScanRate(rate: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE,
                rate
            )
            _refineScanRate.postValue(rate)
        }
    }

    fun setSwitchHoldTime(time: Long) {
        viewModelScope.launch {
            preferenceManager.setLongValue(
                PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME,
                time
            )
            _switchHoldTime.postValue(time)
        }
    }

    fun setPauseScanOnSwitchHold(pause: Boolean) {
        viewModelScope.launch {
            preferenceManager.setBooleanValue(
                PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
                pause
            )
            _pauseScanOnSwitchHold.postValue(pause)
        }
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
}