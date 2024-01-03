package com.enaboapps.switchify.screens.settings.models

import android.content.Context
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.preferences.PreferenceManager

class SettingsScreenModel(context: Context) : ViewModel() {
    private val preferenceManager = PreferenceManager(context)

    fun getScanRate(): Int {
        return preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }

    fun setScanRate(rate: Int) {
        preferenceManager.setIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE, rate)
    }

    fun getSwitchHoldTime(): Int {
        return preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME)
    }

    fun setSwitchHoldTime(time: Int) {
        preferenceManager.setIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SWITCH_HOLD_TIME, time)
    }

    fun getAutoSelect(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }

    fun setAutoSelect(autoSelect: Boolean) {
        preferenceManager.setBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT, autoSelect)
    }

    fun getAutoSelectDelay(): Int {
        return preferenceManager.getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
    }

    fun setAutoSelectDelay(delay: Int) {
        preferenceManager.setIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY, delay)
    }
}