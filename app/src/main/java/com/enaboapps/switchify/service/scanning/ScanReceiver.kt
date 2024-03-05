package com.enaboapps.switchify.service.scanning

object ScanReceiver {
    var state: ReceiverState = ReceiverState.CURSOR

    /**
     * This enum represents the state of the scanning receiver
     */
    enum class ReceiverState {
        /**
         * This state represents the cursor
         */
        CURSOR,

        /**
         * This state represents the item scan
         * Sequentially scanning the items on the screen
         */
        ITEM_SCAN,

        /**
         * This state represents the menu
         */
        MENU
    }
}