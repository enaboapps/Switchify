package com.enaboapps.switchify.screens.models

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
}