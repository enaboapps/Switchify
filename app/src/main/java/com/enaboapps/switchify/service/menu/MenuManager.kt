package com.enaboapps.switchify.service.menu

import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.menus.gestures.GesturesMenu
import com.enaboapps.switchify.service.menu.menus.gestures.SwipeGesturesMenu
import com.enaboapps.switchify.service.menu.menus.gestures.TapGesturesMenu
import com.enaboapps.switchify.service.menu.menus.gestures.ZoomGesturesMenu
import com.enaboapps.switchify.service.menu.menus.main.MainMenu
import com.enaboapps.switchify.service.menu.menus.system.SystemControlMenu
import com.enaboapps.switchify.service.menu.menus.system.VolumeControlMenu
import com.enaboapps.switchify.service.scanning.ScanReceiver
import com.enaboapps.switchify.service.scanning.ScanningManager

/**
 * This class manages the menu
 */
class MenuManager {
    companion object {
        private var instance: MenuManager? = null

        /**
         * This function gets the instance of the menu manager
         */
        fun getInstance(): MenuManager {
            if (instance == null) {
                instance = MenuManager()
            }
            return instance!!
        }
    }

    /**
     * The scanning manager
     */
    private var scanningManager: ScanningManager? = null

    /**
     * The accessibility service
     */
    private var accessibilityService: SwitchifyAccessibilityService? = null

    /**
     * The state of the scan receiver when the menu was activated
     */
    var scanReceiverState: ScanReceiver.ReceiverState = ScanReceiver.ReceiverState.CURSOR

    /**
     * The menu hierarchy
     */
    var menuHierarchy: MenuHierarchy? = null

    /**
     * This function sets up the menu manager
     * @param scanningManager The scanning manager
     * @param accessibilityService The accessibility service
     */
    fun setup(
        scanningManager: ScanningManager,
        accessibilityService: SwitchifyAccessibilityService
    ) {
        this.scanningManager = scanningManager
        menuHierarchy = MenuHierarchy(scanningManager)
        this.accessibilityService = accessibilityService
    }

    /**
     * This function sets the scan receiver state back to the state that activated the menu
     */
    fun resetScanReceiverState() {
        ScanReceiver.state = scanReceiverState
    }

    /**
     * This function changes between cursor and item scan based on the current state
     */
    fun changeBetweenCursorAndItemScan() {
        if (scanReceiverState == ScanReceiver.ReceiverState.CURSOR) {
            scanningManager?.setItemScanState()
        } else {
            scanningManager?.setCursorState()
        }
    }

    /**
     * This function gets the name of the state to switch to (cursor or item scan)
     * @return The name of the state to switch to
     */
    fun getStateToSwitchTo(): String {
        return if (scanReceiverState == ScanReceiver.ReceiverState.CURSOR) {
            "Item Scan"
        } else {
            "Cursor"
        }
    }

    /**
     * This function opens the main menu
     */
    fun openMainMenu() {
        val mainMenu = MainMenu(accessibilityService!!)
        openMenu(mainMenu.build())
    }

    /**
     * This function opens the system control menu
     */
    fun openSystemControlMenu() {
        val systemControlMenu = SystemControlMenu(accessibilityService!!)
        openMenu(systemControlMenu.build())
    }

    /**
     * This function opens the volume control menu
     */
    fun openVolumeControlMenu() {
        val volumeControlMenu = VolumeControlMenu(accessibilityService!!)
        openMenu(volumeControlMenu.build())
    }

    /**
     * This function opens the gestures menu
     */
    fun openGesturesMenu() {
        val gesturesMenu = GesturesMenu(accessibilityService!!)
        openMenu(gesturesMenu.build())
    }

    /**
     * This function opens the tap menu
     */
    fun openTapMenu() {
        val tapGesturesMenu = TapGesturesMenu(accessibilityService!!)
        openMenu(tapGesturesMenu.build())
    }

    /**
     * This function opens the swipe gestures menu
     */
    fun openSwipeMenu() {
        val swipeGesturesMenu = SwipeGesturesMenu(accessibilityService!!)
        openMenu(swipeGesturesMenu.build())
    }

    /**
     * This function opens the zoom gestures menu
     */
    fun openZoomGesturesMenu() {
        val zoomGesturesMenu = ZoomGesturesMenu(accessibilityService!!)
        openMenu(zoomGesturesMenu.build())
    }

    /**
     * This function opens the menu
     * @param menu The menu to open
     */
    private fun openMenu(menu: MenuView) {
        // Add the menu to the hierarchy
        menuHierarchy?.openMenu(menu)
    }

    /**
     * This function closes the menu hierarchy
     */
    fun closeMenuHierarchy() {
        menuHierarchy?.removeAllMenus()
    }
}