package com.enaboapps.switchify.service.menu

import android.content.Context
import android.widget.LinearLayout
import com.enaboapps.switchify.R

/**
 * This class represents a page of the menu
 * @property context The context of the menu page
 * @property menuItems The menu items of the page
 * @property pageIndex The index of the page
 * @property maxPageIndex The maximum index of the page
 * @property onMenuPageChanged The action to perform when the page is changed
 */
class MenuPage(
    val context: Context,
    private val menuItems: List<MenuItem>,
    private val pageIndex: Int,
    private val maxPageIndex: Int,
    val onMenuPageChanged: (pageIndex: Int) -> Unit
) {
    private var linearLayout: LinearLayout = LinearLayout(context)
    private var menuChangeBtn: MenuItem? = null
    private var _menuItems: List<MenuItem> = menuItems

    init {
        linearLayout.orientation = LinearLayout.VERTICAL

        if (maxPageIndex > 0) {
            menuChangeBtn = MenuItem(
                drawableId = R.drawable.ic_change_menu_page,
                closeOnSelect = false,
                action = { changePage() }
            )
            _menuItems += menuChangeBtn!!
        }
    }


    /**
     * Get the menu items of the page
     * @return The menu items of the page
     */
    fun getMenuItems(): List<MenuItem> {
        return _menuItems
    }


    /**
     * Get the layout of the menu
     * @return The layout of the menu
     */
    fun getMenuLayout(): LinearLayout {
        linearLayout.removeAllViews()
        _menuItems.forEach {
            it.inflate(linearLayout)
        }
        return linearLayout
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