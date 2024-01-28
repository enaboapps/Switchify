package com.enaboapps.switchify.service.menu

import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.service.scanning.ScanState
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import com.enaboapps.switchify.service.scanning.ScanningManager
import java.util.Timer
import java.util.TimerTask

interface MenuViewListener {
    fun onMenuViewClosed()
}

class MenuView(
    val context: Context,
    val menuItems: List<MenuItem>
) : ScanStateInterface {

    var menuViewListener: MenuViewListener? = null

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var linearLayout = LinearLayout(context)

    // scanIndex is the index of the menu item that is currently being scanned
    var scanIndex = 0
    var direction: ScanDirection = ScanDirection.DOWN
    // timer is the timer that is used to scan the menu items
    private var timer: Timer? = null
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
        val itemsLessHierarchy = menuItems.filter { !it.isMenuHierarchyManipulator }
        val numOfItemsLessHierarchy = itemsLessHierarchy.size
        numOfPages = (numOfItemsLessHierarchy / numOfItemsPerPage)
        if (numOfItemsLessHierarchy % numOfItemsPerPage != 0) {
            numOfPages++
        }
        for (item in itemsLessHierarchy) {
            item.page = (itemsLessHierarchy.indexOf(item) / numOfItemsPerPage)
        }
        for (i in 0 until numOfPages) {
            val hierarchyItems = menuItems.filter { it.isMenuHierarchyManipulator }
            val items = itemsLessHierarchy.filter { it.page == i } + hierarchyItems
            val containsNonHierarchyItems = items.any { !it.isMenuHierarchyManipulator }
            if (containsNonHierarchyItems) {
                menuPages.add(MenuPage(context, items, i, numOfPages - 1) {
                    onMenuPageChanged(it)
                })
            } else {
                numOfPages--
                break
            }
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
        linearLayout.removeAllViews()
        // Add the menu items to the LinearLayout
        linearLayout.addView(menuPages[currentPage].getMenuLayout())
    }

    private fun createLinearLayout() {
        linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.setPadding(10, 10, 10, 10)
        linearLayout.setBackgroundColor(context.resources.getColor(android.R.color.darker_gray, null))
    }

    private fun addToWindowManager() {
        windowManager.addView(linearLayout, WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            0
        ))
    }


    // This function is called when the menu is opened
    fun open(scanningManager: ScanningManager) {
        // Create the LinearLayout
        createLinearLayout()
        // Inflate the menu
        inflateMenu()
        // Add to the WindowManager
        addToWindowManager()
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
        // Stop scanning
        stopScanning()
        // Remove the LinearLayout from the WindowManager
        try {
            linearLayout.removeAllViews()
            windowManager.removeView(linearLayout)
        } catch (e: Exception) {
            Log.e("MenuView", "Error removing menu view", e)
        }
        // Call the listener
        menuViewListener?.onMenuViewClosed()
    }

    // This function starts scanning the menu items
    private fun startScanning() {
        // Set the first menu item to be highlighted
        getCurrentItem().highlight()
        scanState = ScanState.SCANNING

        val mode = ScanMode.fromId(PreferenceManager(context).getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_MODE))
        if (mode.id == ScanMode.Modes.MODE_MANUAL) {
            return
        }

        val rate = PreferenceManager(context).getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        // If the timer is not null, cancel it
        timer?.cancel()
        timer = null
        // Start the timer
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (scanState == ScanState.SCANNING) {
                    // Unhighlight the current menu item
                    getCurrentItem().unhighlight()
                    // If direction is down, increment scanIndex
                    // If direction is up, decrement scanIndex
                    if (direction == ScanDirection.DOWN) {
                        scanIndex++
                    } else {
                        scanIndex--
                    }
                    // If direction is down and scanIndex is greater than or equal to the number of menu items, set scanIndex to 0
                    // If direction is up and scanIndex is less than 0, set scanIndex to the number of menu items minus 1
                    if (direction == ScanDirection.DOWN && scanIndex >= menuPages[currentPage].getMenuItems().size) {
                        scanIndex = 0
                    } else if (direction == ScanDirection.UP && scanIndex < 0) {
                        scanIndex = menuPages[currentPage].getMenuItems().size - 1
                    }
                    // Highlight the current menu item
                    getCurrentItem().highlight()

                    Log.d("MenuView", "Scanning menu item ${scanIndex}")
                }
            }
        }, rate.toLong(), rate.toLong())
    }

    // This function stops scanning the menu items
    override fun stopScanning() {
        // Cancel the timer
        timer?.cancel()
        timer = null
        // Unhighlight the current menu item
        getCurrentItem().unhighlight()
        // Set the scan state to stopped
        scanState = ScanState.STOPPED
    }

    // This function pauses scanning the menu items
    override fun pauseScanning() {
        if (scanState == ScanState.SCANNING) {
            scanState = ScanState.PAUSED
        }
    }

    // This function resumes scanning the menu items
    override fun resumeScanning() {
        if (scanState == ScanState.PAUSED) {
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