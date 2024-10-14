package com.enaboapps.switchify.service.menu

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

interface MenuViewListener {
    fun onMenuViewClosed()
}

class MenuView(
    val context: Context,
    private val menu: BaseMenu
) {
    var menuViewListener: MenuViewListener? = null

    private val switchifyAccessibilityWindow: SwitchifyAccessibilityWindow =
        SwitchifyAccessibilityWindow.instance

    private var baseLayout = LinearLayout(context)

    // Page variables
    private var currentPage = 0
    private var numOfPages = 0
    private val menuPages = mutableListOf<MenuPage>()

    // Scan tree
    val scanTree = ScanTree(context)

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
        // Number of items per page
        val numOfItemsPerPage = 6
        // Calculate the number of pages
        numOfPages = (menuItems.size + numOfItemsPerPage - 1) / numOfItemsPerPage
        // Create the menu pages
        for (i in 0 until numOfPages) {
            // Calculate the start and end of the interval
            val start = i * numOfItemsPerPage
            val end = ((i + 1) * numOfItemsPerPage).coerceAtMost(menuItems.size)

            // Get the items for the page
            val pageItems = menuItems.subList(start, end)
            val navRowItems = menu.buildNavMenuItems()
            val systemNavItems = menu.buildSystemNavItems()

            // Create the rows
            // Add the system navigation row to the beginning
            val rows = mutableListOf(systemNavItems)
            // Split the items into rows of 3
            pageItems.chunked(3).forEach { rowItems ->
                rows.add(rowItems)
            }

            // Add the menu page
            menuPages.add(
                MenuPage(
                    context,
                    rows,
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
        // Set the current page to the new page
        currentPage = pageIndex
        // Inflate the menu
        inflateMenu()
    }

    // This function inflates the menu
    private fun inflateMenu() {
        // Clear the scan tree
        scanTree.clearTree()
        // Remove all views from the LinearLayout
        baseLayout.removeAllViews()

        val pageExists = currentPage < menuPages.size
        // If the page exists, add the menu items to the LinearLayout
        if (pageExists) {
            baseLayout.addView(menuPages[currentPage].getMenuLayout())
        }

        // Build the scan tree after half a second if the page exists, otherwise, close the menu hierarchy
        Handler(Looper.getMainLooper()).postDelayed({
            if (pageExists) {
                scanTree.buildTree(menuPages[currentPage].translateMenuItemsToNodes(), 0)

                // Perform the start scanning action if it is enabled in the app settings
                val scanSettings = ScanSettings(context)
                if (scanSettings.getAutomaticallyStartScanAfterSelection()) {
                    scanTree.startScanning()
                }
            } else {
                MenuManager.getInstance().closeMenuHierarchy()
            }
        }, 500)
    }

    private fun createLinearLayout() {
        baseLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(context.resources.getColor(R.color.navy, null))
        }
    }

    private fun addToWindow() {
        switchifyAccessibilityWindow.addView(baseLayout, Int.MAX_VALUE, Int.MAX_VALUE)

        // Set the menu viewTreeObserver
        baseLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val width = baseLayout.width
            val height = baseLayout.height

            val screenWidth = context.resources.displayMetrics.widthPixels
            val screenHeight = context.resources.displayMetrics.heightPixels

            val point = GesturePoint.getPoint()

            // Set the menu position to be above or below the point making sure it fits on the screen
            val x = if (point.x + width > screenWidth) {
                screenWidth - width.toFloat()
            } else {
                point.x
            }
            val y = if (point.y + height > screenHeight) {
                screenHeight - height.toFloat()
            } else {
                point.y
            }
            switchifyAccessibilityWindow.updateViewLayout(baseLayout, x.toInt(), y.toInt())
        }
    }


    // This function is called when the menu is opened
    fun open(scanningManager: ScanningManager) {
        // Set the menu type
        scanningManager.setMenuType()
        // Create the LinearLayout
        createLinearLayout()
        // Add to the window
        addToWindow()
        // Inflate the menu
        inflateMenu()
    }

    // This function is called when the menu is closed
    fun close() {
        // Remove the LinearLayout from the window
        baseLayout.removeAllViews()
        switchifyAccessibilityWindow.removeView(baseLayout)
        // Shutdown the scan tree
        scanTree.shutdown()
        // Call the listener
        menuViewListener?.onMenuViewClosed()
    }
}