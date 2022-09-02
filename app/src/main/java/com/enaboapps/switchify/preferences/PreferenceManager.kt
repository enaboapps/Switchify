package com.enaboapps.switchify.preferences

import android.content.Context
import com.beust.klaxon.Klaxon

class PreferenceManager(private val context: Context) {

    object Keys {
        const val PREFERENCE_KEY_SCAN_RATE = "scan_rate"
    }

    // Preference file name
    private val PREFERENCE_FILE_NAME = "switchify_preferences.json"

    // Preferences map
    private var preferences = mutableMapOf<String, Any>()

    // Function to read preferences json file from the device
    private fun readPreferencesFile() {
        val rootFolder = context.filesDir
        val preferencesFile = rootFolder.resolve(PREFERENCE_FILE_NAME)
        if (preferencesFile.exists()) {
            val jsonString = preferencesFile.readText()
            if (jsonString.isNotEmpty()) {
                preferences = Klaxon().parse<MutableMap<String, Any>>(jsonString) ?: mutableMapOf()
            }
        } else {
            createPreferencesFile()
        }
    }

    // Function to create preferences json file and save it to the device
    private fun createPreferencesFile() {
        val rootFolder = context.filesDir
        val preferencesFile = rootFolder.resolve(PREFERENCE_FILE_NAME)
        if (!preferencesFile.exists()) {
            preferencesFile.createNewFile()
        }
    }

    // Function to save preferences to the device
    private fun savePreferences() {
        val rootFolder = context.filesDir
        val preferencesFile = rootFolder.resolve(PREFERENCE_FILE_NAME)
        val jsonString = Klaxon().toJsonString(preferences)
        preferencesFile.writeText(jsonString)
    }


    // Function to set integer value to the preferences map
    fun setIntegerValue(key: String, value: Int) {
        preferences[key] = value
        savePreferences()
    }

    // Function to set float value to the preferences map
    fun setFloatValue(key: String, value: Float) {
        preferences[key] = value
        savePreferences()
    }

    // Function to set boolean value to the preferences map
    fun setBooleanValue(key: String, value: Boolean) {
        preferences[key] = value
        savePreferences()
    }

    // Function to get float value from the preferences map
    fun getFloatValue(key: String): Float {
        readPreferencesFile()
        return if (preferences.containsKey(key)) {
            preferences[key] as Float
        } else {
            0f
        }
    }

    // Function to get boolean value from the preferences file
    fun getBooleanValue(key: String): Boolean {
        readPreferencesFile()
        return if (preferences.containsKey(key)) {
            preferences[key] as Boolean
        } else {
            false
        }
    }

    // Function to get integer value from the preferences map
    fun getIntegerValue(key: String): Int {
        readPreferencesFile()
        return if (preferences.containsKey(key)) {
            preferences[key] as Int
        } else {
            1000
        }
    }

}