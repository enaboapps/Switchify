package com.enaboapps.switchify.service.menu

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.scanning.ScanTree
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.utils.ScreenUtils
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
        val numOfItemsPerPage: Int
        val ballparkHeight = 150
        val screenHeight = ScreenUtils.getHeight(context)
        numOfItemsPerPage = (screenHeight / ballparkHeight).coerceAtLeast(1)
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
        // Add the menu items to the LinearLayout
        baseLayout.addView(menuPages[currentPage].getMenuLayout())
        // Build the scan tree after half a second
        Handler(Looper.getMainLooper()).postDelayed({
            scanTree.buildTree(menuPages[currentPage].getMenuItems(), 0)
        }, 500)
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
        // Create the LinearLayout
        createLinearLayout()
        // Add to the window
        addToWindow()
        // Inflate the menu
        inflateMenu()
        // Set the menu state
        scanningManager.setMenuState()
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