package com.enaboapps.switchify.screens.settings.models

import android.content.Context
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.preferences.PreferenceManager

class SettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    fun getScanRate(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }

    fun setScanRate(rate: Long) {
        preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
    }

    fun getRefineScanRate(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE)
    }

    fun setRefineScanRate(rate: Long) {
        preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_REFINE_SCAN_RATE, rate)
    }

    fun getSwitchHoldTime(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME)
    }

    fun setSwitchHoldTime(time: Long) {
        preferenceManager.setLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME, time)
    }

    fun getPauseScanOnSwitchHold(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD)
    }

    fun setPauseScanOnSwitchHold(pause: Boolean) {
        preferenceManager.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD,
            pause
        )
    }

    fun getAutoSelect(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }

    fun setAutoSelect(autoSelect: Boolean) {
        preferenceManager.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT,
            autoSelect
        )
    }

    fun getAutoSelectDelay(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
    }

    fun setAutoSelectDelay(delay: Long) {
        preferenceManager.setLongValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY,
            delay
        )
    }
}