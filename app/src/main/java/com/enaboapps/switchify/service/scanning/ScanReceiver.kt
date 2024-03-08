package com.enaboapps.switchify.service.scanning

import com.enaboapps.switchify.preferences.PreferenceManager

object ScanReceiver {
    var preferenceManager: PreferenceManager? = null

    /**
     * This variable is used to determine if the scanning is in the menu
     */
    var isInMenu = false

    /**
     * This enum represents the state of the scanning receiver
     */
    object ReceiverState {
        /**
         * This state represents the cursor
         */
        const val CURSOR = 0

        /**
         * This state represents the item scan
         * Sequentially scanning the items on the screen
         */
        const val ITEM_SCAN = 1
    }

    /**
     * This function is used to get the state of the scanning receiver
     */
    fun getState(): Int {
        preferenceManager?.let { preferenceManager ->
            val storedState = preferenceManager.getIntegerValue(
                PreferenceManager.PREFERENCE_KEY_SCAN_RECEIVER
            )
            println("Stored state: $storedState")
            return if (storedState == ReceiverState.CURSOR || storedState == ReceiverState.ITEM_SCAN) {
                storedState
            } else {
                ReceiverState.CURSOR
            }
        }
        return ReceiverState.CURSOR
    }

    /**
     * This function is used to set the state of the scanning receiver
     */
    fun setState(value: Int) {
        preferenceManager?.setIntegerValue(
            PreferenceManager.PREFERENCE_KEY_SCAN_RECEIVER,
            value
        )
    }
}