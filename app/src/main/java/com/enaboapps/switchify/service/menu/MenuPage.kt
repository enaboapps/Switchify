package com.enaboapps.switchify.service.menu

import android.content.Context
import android.widget.LinearLayout
import com.enaboapps.switchify.R

/**
 * This class represents a page of the menu
 * @property context The context of the menu page
 * @property menuItems The menu items of the page
 * @property navRowItems The navigation row items of the page
 * @property pageIndex The index of the page
 * @property maxPageIndex The maximum index of the page
 * @property onMenuPageChanged The action to perform when the page is changed
 */
class MenuPage(
    val context: Context,
    private val menuItems: List<MenuItem>,
    private val navRowItems: List<MenuItem>,
    private val pageIndex: Int,
    private val maxPageIndex: Int,
    val onMenuPageChanged: (pageIndex: Int) -> Unit
) {
    private var baseLayout: LinearLayout = LinearLayout(context)
    private var navRowLayout = LinearLayout(context)
    private var menuChangeBtn: MenuItem? = null
    private var _navRowItems: MutableList<MenuItem> = navRowItems.toMutableList()

    init {
        baseLayout.orientation = LinearLayout.VERTICAL
        navRowLayout.orientation = LinearLayout.HORIZONTAL
        navRowLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        if (maxPageIndex > 0) {
            menuChangeBtn = MenuItem(
                drawableId = R.drawable.ic_change_menu_page,
                drawableDescription = "Change menu page",
                closeOnSelect = false,
                action = { changePage() }
            )
            _navRowItems += menuChangeBtn!!
        }
    }


    /**
     * Get the menu items of the page
     * @return The menu items of the page including the navigation row items
     */
    fun getMenuItems(): List<MenuItem> {
        return menuItems + _navRowItems
    }


    /**
     * Get the layout of the menu
     * @return The layout of the menu
     */
    fun getMenuLayout(): LinearLayout {
        baseLayout.removeAllViews()
        navRowLayout.removeAllViews()
        menuItems.forEach { menuItem ->
            menuItem.inflate(baseLayout)
        }
        _navRowItems.forEach { menuItem ->
            menuItem.inflate(navRowLayout, true)
        }
        baseLayout.addView(navRowLayout)
        return baseLayout
    }


    /**
     * Change the page
     */
    private fun changePage() {
        var newPageIndex = pageIndex
        if (pageIndex == maxPageIndex) {
            newPageIndex = 0
        } else {
            newPageIndex++
        }
        onMenuPageChanged(newPageIndex)
    }
}