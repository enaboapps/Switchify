package com.enaboapps.switchify.service.scanning

import com.enaboapps.switchify.preferences.PreferenceManager

object ScanMethod {
    var preferenceManager: PreferenceManager? = null

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
        const val CURSOR = 0

        /**
         * This type represents the item scan
         * Sequentially scanning the items on the screen
         */
        const val ITEM_SCAN = 1
    }

    /**
     * This function is used to get the type of the scanning method
     */
    fun getType(): Int {
        preferenceManager?.let { preferenceManager ->
            val storedType = preferenceManager.getIntegerValue(
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
     * This function is used to set the type of the scanning method
     */
    fun setType(value: Int) {
        preferenceManager?.setIntegerValue(
            PreferenceManager.PREFERENCE_KEY_SCAN_METHOD,
            value
        )
    }
}