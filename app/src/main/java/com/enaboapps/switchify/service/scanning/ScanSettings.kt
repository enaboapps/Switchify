package com.enaboapps.switchify.service.scanning

import android.content.Context
import com.enaboapps.switchify.preferences.PreferenceManager

/**
 * A convenience class to get the scan settings
 * @param context The context to use
 */
class ScanSettings(context: Context) {
    private val preferenceManager = PreferenceManager(context)

    /**
     * Check if the scan mode is auto
     * @return true if the scan mode is auto, false otherwise
     */
    fun isAutoScanMode(): Boolean {
        return ScanMode.fromId(preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE)).id == ScanMode.Modes.MODE_AUTO
    }

    /**
     * Check if the scan mode is manual
     * @return true if the scan mode is manual, false otherwise
     */
    fun isManualScanMode(): Boolean {
        return ScanMode.fromId(preferenceManager.getStringValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE)).id == ScanMode.Modes.MODE_MANUAL
    }

    /**
     * Get the scan rate
     * @return The scan rate
     */
    fun getScanRate(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
    }

    /**
     * Get the cursor scan rate
     * @return The cursor scan rate
     */
    fun getCursorBlockScanRate(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_CURSOR_BLOCK_SCAN_RATE)
    }

    /**
     * Get the fine cursor scan rate
     * @return The fine cursor scan rate
     */
    fun getFineCursorScanRate(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_CURSOR_FINE_SCAN_RATE)
    }

    /**
     * Get the radar scan rate
     * @return The radar scan rate
     */
    fun getRadarScanRate(): Long {
        return preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_RADAR_SCAN_RATE)
    }

    /**
     * Check if the pause on first item is enabled
     * @return true if the pause on first item is enabled, false otherwise
     */
    fun isPauseOnFirstItemEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM)
    }

    /**
     * Get the pause on first item delay
     * @return The pause on first item delay if enabled, 0 otherwise
     */
    fun getPauseOnFirstItemDelay(): Long {
        return if (isPauseOnFirstItemEnabled()) {
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_PAUSE_ON_FIRST_ITEM_DELAY)
        } else {
            0
        }
    }

    /**
     * Check if the auto select is enabled
     * @return true if the auto select is enabled, false otherwise
     */
    fun isAutoSelectEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT)
    }

    /**
     * Get the auto select delay
     * @return The auto select delay if enabled, 0 otherwise
     */
    fun getAutoSelectDelay(): Long {
        return if (isAutoSelectEnabled()) {
            preferenceManager.getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_AUTO_SELECT_DELAY)
        } else {
            0
        }
    }

    /**
     * Check if row column scan is enabled
     * @return true if row column scan is enabled, false otherwise
     */
    fun isRowColumnScanEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_ROW_COLUMN_SCAN)
    }

    /**
     * Check if group scan is enabled
     * @return true if group scan is enabled, false otherwise
     */
    fun isGroupScanEnabled(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.Keys.PREFERENCE_KEY_GROUP_SCAN)
    }

    /**
     * Check if the pause scan on switch hold is required
     * @return true if the rates are <= 0.4 seconds, false otherwise
     */
    fun isPauseScanOnSwitchHoldRequired(): Boolean {
        // 0.4 seconds is the threshold for pausing scan on switch hold
        val threshold = 400L // in milliseconds
        val result =
            getScanRate() <= threshold && getCursorBlockScanRate() <= threshold && getFineCursorScanRate() <= threshold
        println("isPauseScanOnSwitchHoldRequired: $result")
        return result
    }
}