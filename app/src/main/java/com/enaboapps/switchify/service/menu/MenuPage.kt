package com.enaboapps.switchify.service.menu

import android.content.Context
import android.widget.LinearLayout
import com.enaboapps.switchify.R

/**
 * This class represents a page of the menu
 * @property context The context of the menu page
 * @property menuItems The menu items of the page
 * @property rowsOfMenuItems The rows of menu items
 * @property pageIndex The index of the page
 * @property maxPageIndex The maximum index of the page
 * @property onMenuPageChanged The action to perform when the page is changed
 */
class MenuPage(
    val context: Context,
    private val menuItems: List<MenuItem>,
    private val rowsOfMenuItems: List<List<MenuItem>>,
    private val pageIndex: Int,
    private val maxPageIndex: Int,
    val onMenuPageChanged: (pageIndex: Int) -> Unit
) {
    private var baseLayout: LinearLayout = LinearLayout(context)
    private var menuChangeBtn: MenuItem? = null

    private var rowsLayout: LinearLayout = LinearLayout(context)


    init {
        baseLayout.orientation = LinearLayout.VERTICAL
        rowsLayout.orientation = LinearLayout.VERTICAL
        rowsLayout.layoutParams = LinearLayout.LayoutParams(
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
            menuChangeBtn?.inflate(baseLayout)
        }
    }


    /**
     * Get the menu items of the page
     * @return The menu items of the page including the row items
     */
    fun getMenuItems(): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        items.addAll(menuItems)
        items.addAll(rowsOfMenuItems.flatten())
        return items
    }


    /**
     * Get the layout of the menu
     * @return The layout of the menu
     */
    fun getMenuLayout(): LinearLayout {
        baseLayout.removeAllViews()
        rowsLayout.removeAllViews()
        menuItems.forEach { it.inflate(baseLayout) }
        rowsOfMenuItems.forEach { rowItems ->
            val rowLayout = LinearLayout(context)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowItems.forEach { menuItem ->
                menuItem.inflate(rowLayout, wrapHorizontal = true)
            }
            rowsLayout.addView(rowLayout)
        }
        baseLayout.addView(rowsLayout)
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