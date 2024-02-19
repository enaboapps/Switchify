package com.enaboapps.switchify.screens.settings.switches.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.preferences.PreferenceManager

class SwitchStabilityScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    private val _pauseScanOnSwitchHold = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD)
    }
    val pauseScanOnSwitchHold: LiveData<Boolean> = _pauseScanOnSwitchHold

    private val _switchIgnoreRepeat = MutableLiveData<Boolean>().apply {
        value =
            preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT)
    }
    val switchIgnoreRepeat: LiveData<Boolean> = _switchIgnoreRepeat

    private val _switchIgnoreRepeatDelay = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY)
    }
    val switchIgnoreRepeatDelay: LiveData<Long> = _switchIgnoreRepeatDelay

    private val _switchHoldTime = MutableLiveData<Long>().apply {
        value =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME)
    }
    val switchHoldTime: LiveData<Long> = _switchHoldTime


    fun setPauseScanOnSwitchHold(value: Boolean) {
        preferenceManager.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
            value
        )
        _pauseScanOnSwitchHold.postValue(value)
    }

    fun setSwitchIgnoreRepeat(value: Boolean) {
        preferenceManager.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT,
            value
        )
        _switchIgnoreRepeat.postValue(value)
    }

    fun setSwitchIgnoreRepeatDelay(value: Long) {
        preferenceManager.setLongValue(
            PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY,
            value
        )
        _switchIgnoreRepeatDelay.postValue(value)
    }

    fun setSwitchHoldTime(value: Long) {
        preferenceManager.setLongValue(
            PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME,
            value
        )
        _switchHoldTime.postValue(value)
    }

    /**
     * Determines if the "Pause scan on switch hold" setting should be shown
     * Checks if the scan rate or refine scan rate is greater than a threshold
     * @return true if the setting should be shown, false otherwise
     */
    fun shouldShowPauseScanOnSwitchHold(): Boolean {
        val scanRate =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        val refineScanRate =
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
        val threshold = 400
        return scanRate > threshold || refineScanRate > threshold
    }
}