package com.enaboapps.switchify.service.techniques.itemscan

import android.content.Context
import com.enaboapps.switchify.backend.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager

/**
 * Settings class for item scanning technique
 * Handles all item scan-specific configuration and preferences
 */
object ItemScanSettings {

    private var preferenceManager: PreferenceManager? = null

    fun init(context: Context) {
        preferenceManager = PreferenceManager(context)
    }

    /**
     * Check if item scan speech is enabled
     * @return true if item scan speech is enabled, false otherwise
     */
    fun isSpeechEnabled(): Boolean {
        return preferenceManager?.getBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_ITEM_SCAN_SPEECH,
            false
        ) ?: false
    }

    /**
     * Set item scan speech enabled
     * @param enabled Whether to enable speech
     */
    fun setSpeechEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_ITEM_SCAN_SPEECH,
            enabled
        )
    }

    /**
     * Check if the automatically start scan after selection is enabled
     * @return true if automatically start scan after selection is enabled, false otherwise
     */
    fun isAutomaticallyStartScanAfterSelectionEnabled(): Boolean {
        val autoStartEnabled = preferenceManager?.getBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTOMATICALLY_START_SCAN_AFTER_SELECTION
        ) ?: false

        // Only enable if auto scan mode and gesture lock is not enabled
        return autoStartEnabled &&
                isAutoScanMode() &&
                !GestureManager.instance.isGestureLockEnabled()
    }

    /**
     * Set automatically start scan after selection
     * @param enabled Whether to enable automatic scan restart
     */
    fun setAutomaticallyStartScanAfterSelectionEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTOMATICALLY_START_SCAN_AFTER_SELECTION,
            enabled
        )
    }

    /**
     * Check if the pause on first item is enabled
     * @return true if the pause on first item is enabled, false otherwise
     */
    fun isPauseOnFirstItemEnabled(): Boolean {
        return preferenceManager?.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM)
            ?: false
    }

    /**
     * Set pause on first item
     * @param enabled Whether to enable pause on first item
     */
    fun setPauseOnFirstItemEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM,
            enabled
        )
    }

    /**
     * Get the pause on first item delay
     * @return The pause on first item delay if enabled, 0 otherwise
     */
    fun getPauseOnFirstItemDelay(): Long {
        return if (isPauseOnFirstItemEnabled()) {
            preferenceManager?.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY)
                ?: 0L
        } else {
            0L
        }
    }

    /**
     * Set the pause on first item delay
     * @param delay The delay in milliseconds
     */
    fun setPauseOnFirstItemDelay(delay: Long) {
        preferenceManager?.setLongValue(
            PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY,
            delay
        )
    }

    /**
     * Check if the auto select is enabled
     * @return true if the auto select is enabled, false otherwise
     */
    fun isAutoSelectEnabled(): Boolean {
        return preferenceManager?.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
            ?: false
    }

    /**
     * Set auto select enabled
     * @param enabled Whether to enable auto select
     */
    fun setAutoSelectEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT,
            enabled
        )
    }

    /**
     * Get the auto select delay
     * @return The auto select delay if enabled, 0 otherwise
     */
    fun getAutoSelectDelay(): Long {
        return if (isAutoSelectEnabled()) {
            preferenceManager?.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
                ?: 0L
        } else {
            0L
        }
    }

    /**
     * Set the auto select delay
     * @param delay The delay in milliseconds
     */
    fun setAutoSelectDelay(delay: Long) {
        preferenceManager?.setLongValue(
            PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY,
            delay
        )
    }

    /**
     * Check if directly select keyboard keys is enabled
     * @return true if directly select keyboard keys is enabled, false otherwise
     */
    fun isDirectlySelectKeyboardKeysEnabled(): Boolean {
        return preferenceManager?.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_DIRECTLY_SELECT_KEYBOARD_KEYS)
            ?: false
    }

    /**
     * Set directly select keyboard keys enabled
     * @param enabled Whether to enable direct keyboard key selection
     */
    fun setDirectlySelectKeyboardKeysEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_DIRECTLY_SELECT_KEYBOARD_KEYS,
            enabled
        )
    }

    /**
     * Check if row column scan is enabled
     * @return true if row column scan is enabled, false otherwise
     */
    fun isRowColumnScanEnabled(): Boolean {
        return preferenceManager?.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN)
            ?: false
    }

    /**
     * Set row column scan enabled
     * @param enabled Whether to enable row column scan
     */
    fun setRowColumnScanEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN,
            enabled
        )
    }

    /**
     * Check if group scan is enabled
     * @return true if group scan is enabled, false otherwise
     */
    fun isGroupScanEnabled(): Boolean {
        return preferenceManager?.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_GROUP_SCAN)
            ?: false
    }

    /**
     * Set group scan enabled
     * @param enabled Whether to enable group scan
     */
    fun setGroupScanEnabled(enabled: Boolean) {
        preferenceManager?.setBooleanValue(
            PreferenceManager.Keys.PREFERENCE_KEY_GROUP_SCAN,
            enabled
        )
    }

    // Helper method to check scan mode - could be moved to a shared utility if needed
    private fun isAutoScanMode(): Boolean {
        val scanModeId =
            preferenceManager?.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE) ?: ""
        return scanModeId == "auto"
    }
}
