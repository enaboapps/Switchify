package com.enaboapps.switchify.service.scanning

import com.enaboapps.switchify.preferences.PreferenceManager

/**
 * This interface is used to observe the changes in the scanning method
 */
interface ScanMethodObserver {
    fun onScanMethodChanged(scanMethod: String)
}

/**
 * This object is used to manage the scanning method
 */
object ScanMethod {
    var preferenceManager: PreferenceManager? = null

    /**
     * This is the observer of the scanning method
     */
    var observer: ScanMethodObserver? = null

    /**
     * This variable is used to determine if the scanning is in the menu
     */
    var isInMenu = false

    /**
     * This enum represents the type of the scanning method
     */
    object MethodType {
        /**
         * This type represents the cursor
         */
        const val CURSOR = "cursor"

        /**
         * This type represents the item scan
         * Sequentially scanning the items on the screen
         */
        const val ITEM_SCAN = "item_scan"
    }

    /**
     * This function is used to get the type of the scanning method
     */
    fun getType(): String {
        preferenceManager?.let { preferenceManager ->
            val storedType = preferenceManager.getStringValue(
                PreferenceManager.PREFERENCE_KEY_SCAN_METHOD
            )
            println("Stored type: $storedType")
            return if (storedType == MethodType.CURSOR || storedType == MethodType.ITEM_SCAN) {
                storedType
            } else {
                MethodType.CURSOR
            }
        }
        return MethodType.CURSOR
    }

    /**
     * This function gets the name of the scanning method
     * @param type The type of the scanning method
     * @return The name of the scanning method
     */
    fun getName(type: String): String {
        return when (type) {
            MethodType.CURSOR -> "Cursor"
            MethodType.ITEM_SCAN -> "Item Scan"
            else -> "Unknown"
        }
    }

    /**
     * This function gets the description of the scanning method
     * @param type The type of the scanning method
     * @return The description of the scanning method
     */
    fun getDescription(type: String): String {
        return when (type) {
            MethodType.CURSOR -> "Cursor allows you to select items by moving a set of crosshairs over the screen."
            MethodType.ITEM_SCAN -> "Item Scan allows you to select items by scanning through them sequentially."
            else -> "Unknown"
        }
    }

    /**
     * This function is used to set the type of the scanning method
     */
    fun setType(value: String) {
        preferenceManager?.setStringValue(
            PreferenceManager.PREFERENCE_KEY_SCAN_METHOD,
            value
        )
        observer?.onScanMethodChanged(value)
    }
}