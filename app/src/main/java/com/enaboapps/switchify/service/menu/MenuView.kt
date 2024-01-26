package com.enaboapps.switchify.service.menu

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanMode
import com.enaboapps.switchify.service.scanning.ScanState
import com.enaboapps.switchify.service.scanning.ScanStateInterface
import java.util.Timer
import java.util.TimerTask

interface MenuViewListener {
    fun onMenuViewClosed()
}

class MenuView(
    val context: Context,
    var menuItems: List<MenuItem>
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
    private data class MenuItemPage(var menuItems: List<MenuItem>, val page: Int)
    private var menuItemPages = mutableListOf<MenuItemPage>()

    constructor(page: Int, context: Context, menuItems: List<MenuItem>) : this(context, menuItems) {
        currentPage = page
    }

    init {
        setup()
    }

    // This function sets up the menu
    private fun setup() {
        // Set the number of pages
        numOfPages = (menuItems.size / numOfItemsPerPage) + 1
        // Iterate through the menu items and set the page
        for (menuItem in menuItems) {
            menuItem.page = (menuItems.indexOf(menuItem) / numOfItemsPerPage) + 1
        }
        // Iterate through the pages and add the menu items to the page
        for (i in 1..numOfPages) {
            menuItemPages.add(MenuItemPage(menuItems.filter { it.page == i }, i))
        }
        // Add a "Next" menu item to each page
        for (i in 1..numOfPages) {
            val menuItem = MenuItem("Next", false) {
                moveToNextPage()
            }
            menuItem.isPageNavItem = true
            menuItemPages[i - 1].menuItems += menuItem
        }
        // Set the menu items to the current page
        menuItems = menuItemPages[currentPage].menuItems
    }

    // This function moves to the next page
    private fun moveToNextPage() {
        // If the current page is less than the number of pages, increment the current page
        if (currentPage < numOfPages - 1) {
            currentPage++
        } else {
            // If the current page is at the last page, set the current page to 0
            currentPage = 0
        }
        // Replace the menu in the hierarchy with the new menu
        val itemsWithoutPageNav = menuItemPages[currentPage].menuItems.filter { !it.isPageNavItem }
        val newMenu = MenuView(currentPage, context, itemsWithoutPageNav)
        MenuManager.getInstance().menuHierarchy?.replaceTopMenu(newMenu)
    }

    private fun createLinearLayout() {
        try {
            windowManager.removeView(linearLayout)
        } catch (e: Exception) {
            Log.e("MenuView", "Error removing menu view", e)
        }
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
    fun open() {
        createLinearLayout()
        // Iterate through the menu items and inflate them
        for (menuItem in menuItems) {
            menuItem.inflate(linearLayout)
        }
        // Add to the WindowManager
        addToWindowManager()
        // Reset the menu
        reset()
    }

    // This function resets the menu
    private fun reset() {
        // Stop scanning
        stopScanning()
        // Unhighlight the current menu item
        menuItems[scanIndex].unhighlight()
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
        menuItems[scanIndex].highlight()
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
                    menuItems[scanIndex].unhighlight()
                    // If direction is down, increment scanIndex
                    // If direction is up, decrement scanIndex
                    if (direction == ScanDirection.DOWN) {
                        scanIndex++
                    } else {
                        scanIndex--
                    }
                    // If direction is down and scanIndex is greater than or equal to the number of menu items, set scanIndex to 0
                    // If direction is up and scanIndex is less than 0, set scanIndex to the number of menu items minus 1
                    if (direction == ScanDirection.DOWN && scanIndex >= menuItems.size) {
                        scanIndex = 0
                    } else if (direction == ScanDirection.UP && scanIndex < 0) {
                        scanIndex = menuItems.size - 1
                    }
                    // Highlight the current menu item
                    menuItems[scanIndex].highlight()

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
        menuItems[scanIndex].unhighlight()
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

    // This function either starts scanning or selects the current menu item
    fun select() {
        Log.d("MenuView", "Selecting menu item $scanIndex")
        if (isScanning()) {
            menuItems[scanIndex].select()
            if (menuItems[scanIndex].closeOnSelect) {
                stopScanning()
                if (!menuItems[scanIndex].isMenuNavItem) {
                    MenuManager.getInstance().menuHierarchy?.removeAllMenus()
                } else {
                    close()
                }
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
            menuItems[scanIndex].unhighlight()
            if (scanIndex < menuItems.size - 1) {
                // If scanIndex is less than the number of menu items minus 1, increment scanIndex
                scanIndex++
            } else {
                // If scanIndex is equal to the number of menu items minus 1, set scanIndex to 0
                scanIndex = 0
            }
            // Highlight the current menu item
            menuItems[scanIndex].highlight()
        } else if (scanState == ScanState.STOPPED) {
            scanIndex = 0
            menuItems[scanIndex].highlight()
            scanState = ScanState.SCANNING
        }
    }

    // This function moves to the previous menu item
    fun moveToPreviousItem() {
        if (isScanning()) {
            // Unhighlight the current menu item
            menuItems[scanIndex].unhighlight()
            if (scanIndex > 0) {
                // If scanIndex is greater than 0, decrement scanIndex
                scanIndex--
            } else {
                // If scanIndex is equal to 0, set scanIndex to the number of menu items minus 1
                scanIndex = menuItems.size - 1
            }
            // Highlight the current menu item
            menuItems[scanIndex].highlight()
        } else if (scanState == ScanState.STOPPED) {
            scanIndex = 0
            menuItems[scanIndex].highlight()
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