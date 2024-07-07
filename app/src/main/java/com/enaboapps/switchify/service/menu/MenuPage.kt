package com.enaboapps.switchify.service.menu

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import com.enaboapps.switchify.R

/**
 * This class represents a page of the menu
 * @property context The context of the menu page
 * @property rowsOfMenuItems The rows of menu items
 * @property pageIndex The index of the page
 * @property maxPageIndex The maximum index of the page
 * @property onMenuPageChanged The action to perform when the page is changed
 */
class MenuPage(
    val context: Context,
    private val rowsOfMenuItems: List<List<MenuItem>>,
    private val pageIndex: Int,
    private val maxPageIndex: Int,
    val onMenuPageChanged: (pageIndex: Int) -> Unit
) {
    private var baseLayout: LinearLayout = LinearLayout(context)
    private var menuChangeBtn: MenuItem? = null


    init {
        baseLayout.orientation = LinearLayout.VERTICAL
        baseLayout.layoutParams = LinearLayout.LayoutParams(
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
     * @return The menu items of the page
     */
    fun getMenuItems(): List<MenuItem> {
        return rowsOfMenuItems.flatten()
    }


    /**
     * Get the layout of the menu
     * @return The layout of the menu
     */
    fun getMenuLayout(): LinearLayout {
        baseLayout.removeAllViews()
        rowsOfMenuItems.forEach { rowItems ->
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
                gravity = Gravity.CENTER
            }
            rowItems.forEach { menuItem ->
                menuItem.inflate(rowLayout)
            }
            baseLayout.addView(rowLayout)
        }
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