package com.enaboapps.switchify.preferences

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    companion object Keys {
        const val PREFERENCE_KEY_SETUP_COMPLETE = "setup_complete"
        const val PREFERENCE_KEY_SCAN_MODE = "scan_mode"
        const val PREFERENCE_KEY_SCAN_RATE = "scan_rate"
        const val PREFERENCE_KEY_SCAN_METHOD = "scan_method"
        const val PREFERENCE_KEY_REFINE_SCAN_RATE = "refine_scan_rate"
        const val PREFERENCE_KEY_SWITCH_HOLD_TIME = "switch_hold_time"
        const val PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM = "pause_on_first_item"
        const val PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY = "pause_on_first_item_delay"
        const val PREFERENCE_KEY_AUTO_SELECT = "auto_select"
        const val PREFERENCE_KEY_AUTO_SELECT_DELAY = "auto_select_delay"
        const val PREFERENCE_KEY_ASSISTED_SELECTION = "assisted_selection"
        const val PREFERENCE_KEY_RESTRICT_CURSOR_TO_KEYBOARD = "restrict_cursor_to_keyboard"
        const val PREFERENCE_KEY_ROW_COLUMN_SCAN = "row_column_scan"
        const val PREFERENCE_KEY_PAUSE_SCAN_ON_SWITCH_HOLD = "pause_scan_on_switch_hold"
        const val PREFERENCE_KEY_SWITCH_IGNORE_REPEAT = "switch_ignore_repeat"
        const val PREFERENCE_KEY_SWITCH_IGNORE_REPEAT_DELAY = "switch_ignore_repeat_delay"
        private const val PREFERENCE_FILE_NAME = "switchify_preferences"
    }

    private val appContext = context.applicationContext

    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)

    val preferenceSync = PreferenceSync(sharedPreferences)

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

    fun setLongValue(key: String, value: Long) {
        with(sharedPreferences.edit()) {
            putLong(key, value)
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

    fun getLongValue(key: String, defaultValue: Long = 1000L): Long {
        // Due to an old version of the app storing some values as different types, we need to do try/catch
        return try {
            sharedPreferences.getLong(key, defaultValue)
        } catch (e: ClassCastException) {
            defaultValue
        }
    }

}