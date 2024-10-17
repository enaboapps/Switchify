package com.enaboapps.switchify.service.menu

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.scanning.ScanningManager
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

/**
 * Interface for listening to menu view closure events.
 */
interface MenuViewListener {
    /**
     * Called when the menu view is closed.
     */
    fun onMenuViewClosed()
}

/**
 * MenuView class responsible for managing and displaying the menu interface.
 * This class handles the creation, display, and navigation of menu pages,
 * as well as the dynamic resizing of the menu when pages change.
 * It maintains a fixed position on the screen and tracks the maximum width and height encountered.
 *
 * @property context The application context.
 * @property menu The base menu to be displayed.
 */
class MenuView(
    val context: Context,
    private val menu: BaseMenu
) {
    /** Listener for menu view events */
    var menuViewListener: MenuViewListener? = null

    /** Instance of SwitchifyAccessibilityWindow for managing window display */
    private val switchifyAccessibilityWindow: SwitchifyAccessibilityWindow =
        SwitchifyAccessibilityWindow.instance

    /** Base layout for the menu */
    private var baseLayout = LinearLayout(context)

    /** Current page index */
    private var currentPage = 0

    /** Total number of pages */
    private var numOfPages = 0

    /** List of menu pages */
    private val menuPages = mutableListOf<MenuPage>()

    /** Scan tree for managing menu item selection */
    val scanTree = ScanTree(context)

    /** Tracks the maximum width encountered */
    private var maxWidth = 0

    /** Tracks the maximum height encountered */
    private var maxHeight = 0

    init {
        setup()
    }

    /**
     * Sets up the menu by retrieving menu items and creating menu pages.
     */
    private fun setup() {
        val menuItems = menu.getMenuItems()
        createMenuPages(menuItems)
    }

    /**
     * Creates menu pages from the provided list of menu items.
     *
     * @param menuItems List of MenuItem objects to be displayed in the menu.
     */
    private fun createMenuPages(menuItems: List<MenuItem>) {
        val numOfItemsPerPage = 6
        numOfPages = (menuItems.size + numOfItemsPerPage - 1) / numOfItemsPerPage
        for (i in 0 until numOfPages) {
            val start = i * numOfItemsPerPage
            val end = ((i + 1) * numOfItemsPerPage).coerceAtMost(menuItems.size)

            val pageItems = menuItems.subList(start, end)
            val navRowItems = menu.buildNavMenuItems()
            val systemNavItems = menu.buildSystemNavItems()

            val rows = mutableListOf(systemNavItems)
            pageItems.chunked(3).forEach { rowItems ->
                rows.add(rowItems)
            }

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

    /**
     * Callback function triggered when a menu page is changed.
     * Updates the current page index and inflates the new menu page.
     *
     * @param pageIndex The index of the new page.
     */
    private fun onMenuPageChanged(pageIndex: Int) {
        currentPage = pageIndex
        inflateMenu()
    }

    /**
     * Inflates the current menu page and sets up the scan tree.
     * This method is responsible for adding the current page's layout to the base layout
     * and setting up a ViewTreeObserver to handle layout changes.
     */
    private fun inflateMenu() {
        scanTree.clearTree()
        baseLayout.removeAllViews()

        val pageExists = currentPage < menuPages.size
        if (pageExists) {
            val pageLayout = menuPages[currentPage].getMenuLayout()
            baseLayout.addView(
                pageLayout,
                ViewGroup.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT
                )
            )

            pageLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    pageLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    updateMaxDimensions()
                    resizeAndRepositionMenu()
                }
            })
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (pageExists) {
                scanTree.buildTree(menuPages[currentPage].translateMenuItemsToNodes(), 0)
            } else {
                MenuManager.getInstance().closeMenuHierarchy()
            }
        }, 500)
    }

    /**
     * Updates the maximum dimensions based on the current layout size.
     */
    private fun updateMaxDimensions() {
        maxWidth = maxWidth.coerceAtLeast(baseLayout.width)
        maxHeight = maxHeight.coerceAtLeast(baseLayout.height)
    }

    /**
     * Creates the base LinearLayout for the menu.
     * Sets the layout parameters to WRAP_CONTENT for both width and height to allow dynamic resizing.
     */
    private fun createLinearLayout() {
        baseLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(context.resources.getColor(R.color.navy, null))
            layoutParams = ViewGroup.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT
            )
        }
    }

    /**
     * Adds the menu view to the accessibility window.
     * It is initially off screen to ensure the menu is properly sized.
     */
    private fun addToWindow() {
        switchifyAccessibilityWindow.addView(
            baseLayout,
            Int.MAX_VALUE,
            Int.MAX_VALUE
        )
    }

    /**
     * Resizes and repositions the menu on the screen.
     * This method ensures the menu is properly sized,
     * handling both larger and smaller page transitions.
     * It also ensures the menu is positioned correctly on the screen.
     */
    private fun resizeAndRepositionMenu() {
        baseLayout.post {
            val screenWidth = context.resources.displayMetrics.widthPixels
            val screenHeight = context.resources.displayMetrics.heightPixels

            val gesturePoint = GesturePoint.getPoint()
            val x = if (gesturePoint.x + maxWidth > screenWidth) {
                screenWidth - maxWidth.toFloat()
            } else {
                gesturePoint.x
            }
            val y = if (gesturePoint.y + maxHeight > screenHeight) {
                screenHeight - maxHeight.toFloat()
            } else {
                gesturePoint.y
            }

            switchifyAccessibilityWindow.updateViewLayout(
                baseLayout,
                x.toInt(),
                y.toInt(),
                WRAP_CONTENT,
                WRAP_CONTENT
            )
        }
    }

    /**
     * Opens the menu.
     * This method initializes the menu, adds it to the window, and inflates the first page.
     *
     * @param scanningManager The ScanningManager instance to set the menu type.
     */
    fun open(scanningManager: ScanningManager) {
        scanningManager.setMenuType()
        createLinearLayout()
        addToWindow()
        inflateMenu()
    }

    /**
     * Closes the menu and performs necessary cleanup.
     * This method removes the menu from the window, shuts down the scan tree,
     * notifies the listener, and resets the max dimensions.
     */
    fun close() {
        baseLayout.removeAllViews()
        switchifyAccessibilityWindow.removeView(baseLayout)
        scanTree.shutdown()
        menuViewListener?.onMenuViewClosed()
        maxWidth = 0
        maxHeight = 0
    }
}