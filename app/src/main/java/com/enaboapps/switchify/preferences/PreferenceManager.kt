package com.enaboapps.switchify.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    companion object Keys {
        const val PREFERENCE_KEY_SETUP_COMPLETE = "setup_complete"
        const val PREFERENCE_KEY_SCAN_MODE = "scan_mode"
        const val PREFERENCE_KEY_SCAN_RATE = "scan_rate"
        const val PREFERENCE_KEY_REFINE_SCAN_RATE = "refine_scan_rate"
        const val PREFERENCE_KEY_SWITCH_HOLD_TIME = "switch_hold_time"
        const val PREFERENCE_KEY_AUTO_SELECT = "auto_select"
        const val PREFERENCE_KEY_AUTO_SELECT_DELAY = "auto_select_delay"
        private const val PREFERENCE_FILE_NAME = "switchify_preferences"
    }

    private val appContext = context.applicationContext

    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)

    fun setSetupComplete() {
        setBooleanValue(PREFERENCE_KEY_SETUP_COMPLETE, true)
    }

    fun isSetupComplete(): Boolean {
        return getBooleanValue(PREFERENCE_KEY_SETUP_COMPLETE)
    }

    fun setIntegerValue(key: String, value: Int) {
        with(sharedPreferences.edit()) {
            putInt(key, value)
            apply()
        }
    }

    fun setFloatValue(key: String, value: Float) {
        with(sharedPreferences.edit()) {
            putFloat(key, value)
            apply()
        }
    }

    fun setBooleanValue(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun getFloatValue(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getIntegerValue(key: String, defaultValue: Int = 1000): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

}