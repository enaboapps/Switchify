package com.enaboapps.switchify.service.menu

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.service.scanning.ScanState
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.scanning.ScanningScheduler
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

interface MenuViewListener {
    fun onMenuViewClosed()
}

class MenuView(
    val context: Context,
    private val menu: BaseMenu
) : ScanStateInterface {

    var menuViewListener: MenuViewListener? = null

    private val switchifyAccessibilityWindow: SwitchifyAccessibilityWindow =
        SwitchifyAccessibilityWindow.instance

    private var baseLayout = LinearLayout(context)

    // scanIndex is the index of the menu item that is currently being scanned
    private var scanIndex = 0
    private var direction: ScanDirection = ScanDirection.DOWN

    private var scanningScheduler: ScanningScheduler? = null

    // scanState is the state of the scanning
    private var scanState = ScanState.STOPPED

    // Page variables
    private val numOfItemsPerPage = 3
    private var currentPage = 0
    private var numOfPages = 0
    private val menuPages = mutableListOf<MenuPage>()

    init {
        setup()
    }

    // This function sets up the menu
    private fun setup() {
        // Get the menu items
        val menuItems = menu.getMenuItems()
        // Create the menu pages
        createMenuPages(menuItems)
    }

    // This function creates the menu pages
    private fun createMenuPages(menuItems: List<MenuItem>) {
        // Calculate the number of pages
        numOfPages = (menuItems.size + numOfItemsPerPage - 1) / numOfItemsPerPage
        // Create the menu pages
        for (i in 0 until numOfPages) {
            val start = i * numOfItemsPerPage
            val end = ((i + 1) * numOfItemsPerPage).coerceAtMost(menuItems.size)
            val pageItems = menuItems.subList(start, end)
            val navRowItems = menu.buildNavMenuItems()
            menuPages.add(
                MenuPage(
                    context,
                    pageItems,
                    navRowItems,
                    i,
                    numOfPages - 1,
                    ::onMenuPageChanged
                )
            )
        }
    }

    // This function is called when the menu page is changed
    private fun onMenuPageChanged(pageIndex: Int) {
        // Stop scanning
        scanIndex = 0
        stopScanning()
        // Set the current page to the new page
        currentPage = pageIndex
        // Inflate the menu
        inflateMenu()
    }

    // This function inflates the menu
    private fun inflateMenu() {
        // Remove all views from the LinearLayout
        baseLayout.removeAllViews()
        // Add the menu items to the LinearLayout
        baseLayout.addView(menuPages[currentPage].getMenuLayout())
    }

    private fun createLinearLayout() {
        // If the point is close to the center, set the transparency to 0.6
        // This way the user can see the content behind the menu
        val transparency = GestureManager.getInstance().isPointCloseToCenter()
        baseLayout = LinearLayout(context)
        baseLayout.alpha = if (transparency) 0.6f else 1f
        baseLayout.orientation = LinearLayout.VERTICAL
        baseLayout.setPadding(10, 10, 10, 10)
        baseLayout.setBackgroundColor(
            context.resources.getColor(
                android.R.color.darker_gray,
                null
            )
        )
    }

    private fun addToWindow() {
        switchifyAccessibilityWindow.addViewToCenter(baseLayout)
    }


    // This function is called when the menu is opened
    fun open(scanningManager: ScanningManager) {
        // Initialize the scanning scheduler
        scanningScheduler = ScanningScheduler {
            stepAutoScan()
        }
        // Create the LinearLayout
        createLinearLayout()
        // Inflate the menu
        inflateMenu()
        // Add to the window
        addToWindow()
        // Reset the menu
        reset()
        // Set the menu state
        scanningManager.setMenuState()
    }

    // This function resets the menu
    private fun reset() {
        // Stop scanning
        stopScanning()
        // Unhighlight the current menu item
        getCurrentItem().unhighlight()
        // Set scanIndex to 0
        scanIndex = 0
        // Set the scan state to stopped
        scanState = ScanState.STOPPED
        // Set the scan direction to down
        direction = ScanDirection.DOWN
    }

    // This function is called when the menu is closed
    fun close() {
        // Shut down the scanning scheduler
        scanningScheduler?.shutdown()
        // Remove the LinearLayout from the window
        baseLayout.removeAllViews()
        switchifyAccessibilityWindow.removeView(baseLayout)
        // Call the listener
        menuViewListener?.onMenuViewClosed()
    }

    // This function starts scanning the menu items
    private fun startScanning() {
        // Set the first menu item to be highlighted
        getCurrentItem().highlight()
        scanState = ScanState.SCANNING

        val mode =
            ScanMode.fromId(PreferenceManager(context).getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
        if (mode.id == ScanMode.Modes.MODE_MANUAL) {
            return
        }

        val rate =
            PreferenceManager(context).getLongValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)

        scanningScheduler?.startScanning(rate, rate)
    }

    // This function stops scanning the menu items
    override fun stopScanning() {
        // Stop the scanning scheduler
        scanningScheduler?.stopScanning()
        // Unhighlight the current menu item
        getCurrentItem().unhighlight()
        // Set the scan state to stopped
        scanState = ScanState.STOPPED
    }

    // This function pauses scanning the menu items
    override fun pauseScanning() {
        if (scanState == ScanState.SCANNING) {
            // Pause the scanning scheduler
            scanningScheduler?.pauseScanning()
            // Set the scan state to paused
            scanState = ScanState.PAUSED
        }
    }

    // This function resumes scanning the menu items
    override fun resumeScanning() {
        if (scanState == ScanState.PAUSED) {
            // Resume the scanning scheduler
            scanningScheduler?.resumeScanning()
            // Set the scan state to scanning
            scanState = ScanState.SCANNING
        }
    }

    // This function returns whether or not the menu is scanning
    private fun isScanning(): Boolean {
        return scanState == ScanState.SCANNING
    }

    private fun getCurrentItem(): MenuItem {
        return menuPages[currentPage].getMenuItems()[scanIndex]
    }

    private fun stepAutoScan() {
        if (direction == ScanDirection.DOWN) {
            moveToNextItem()
        } else {
            moveToPreviousItem()
        }
    }

    // This function either starts scanning or selects the current menu item
    fun select() {
        val item = getCurrentItem()
        Log.d("MenuView", "Selecting menu item $scanIndex")
        if (isScanning()) {
            item.select()
            if (item.closeOnSelect) {
                close()
            } else {
                stopScanning()
                startScanning()
            }
        } else if (scanState == ScanState.STOPPED) {
            startScanning()
        }
    }

    // This function moves to the next menu item
    fun moveToNextItem() {
        if (isScanning()) {
            // Unhighlight the current menu item
            getCurrentItem().unhighlight()
            if (scanIndex < menuPages[currentPage].getMenuItems().size - 1) {
                // If scanIndex is less than the number of menu items minus 1, increment scanIndex
                scanIndex++
            } else {
                // If scanIndex is equal to the number of menu items minus 1, set scanIndex to 0
                scanIndex = 0
            }
            // Highlight the current menu item
            getCurrentItem().highlight()
        } else if (scanState == ScanState.STOPPED) {
            scanIndex = 0
            getCurrentItem().highlight()
            scanState = ScanState.SCANNING
        }
    }

    // This function moves to the previous menu item
    fun moveToPreviousItem() {
        if (isScanning()) {
            // Unhighlight the current menu item
            getCurrentItem().unhighlight()
            if (scanIndex > 0) {
                // If scanIndex is greater than 0, decrement scanIndex
                scanIndex--
            } else {
                // If scanIndex is equal to 0, set scanIndex to the number of menu items minus 1
                scanIndex = menuPages[currentPage].getMenuItems().size - 1
            }
            // Highlight the current menu item
            getCurrentItem().highlight()
        } else if (scanState == ScanState.STOPPED) {
            scanIndex = menuPages[currentPage].getMenuItems().size - 1
            getCurrentItem().highlight()
            scanState = ScanState.SCANNING
        }
    }

    // This function swaps the scan direction
    fun swapScanDirection() {
        direction = if (direction == ScanDirection.DOWN) {
            ScanDirection.UP
        } else {
            ScanDirection.DOWN
        }

        // If paused, start scanning
        if (scanState == ScanState.PAUSED) {
            startScanning()
        }
    }
}