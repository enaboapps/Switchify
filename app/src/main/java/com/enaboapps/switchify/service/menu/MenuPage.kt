package com.enaboapps.switchify.service.menu

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.enaboapps.switchify.R

/**
 * This class represents a page of the menu
 * @property context The context of the menu page
 * @property rowsOfMenuItems The rows of menu items
 * @property navRowItems The navigation row items
 * @property pageIndex The index of the page
 * @property maxPageIndex The maximum index of the page
 * @property onMenuPageChanged The action to perform when the page is changed
 */
class MenuPage(
    val context: Context,
    private val rowsOfMenuItems: List<List<MenuItem>>,
    private val navRowItems: List<MenuItem>,
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
    }


    /**
     * Get the menu items of the page
     * @return The menu items of the page
     */
    fun getMenuItems(): List<MenuItem> {
        val menuItems = mutableListOf<MenuItem>()
        rowsOfMenuItems.forEach { rowItems ->
            rowItems.forEach { menuItem ->
                menuItems.add(menuItem)
            }
        }
        menuItems.addAll(navRowItems)
        menuChangeBtn?.let { menuItems.add(it) }
        return menuItems
    }


    /**
     * Get the layout of the menu
     * @return The layout of the menu
     */
    fun getMenuLayout(): LinearLayout {
        baseLayout.removeAllViews()

        val margin = 8

        rowsOfMenuItems.forEach { rowItems ->
            val rowLayout = createRowLayout()
            rowItems.forEach { menuItem ->
                menuItem.inflate(rowLayout, margin)
            }
            baseLayout.addView(rowLayout)
        }

        val navButtonView = createNavButtonView()
        navRowItems.forEach { menuItem ->
            menuItem.inflate(navButtonView, margin, 75, 65)
        }

        if (maxPageIndex > 0) {
            menuChangeBtn = MenuItem(
                drawableId = R.drawable.ic_change_menu_page,
                drawableDescription = "Change page",
                closeOnSelect = false,
                action = { changePage() }
            )
            menuChangeBtn?.inflate(navButtonView, margin, 75, 65)
        }

        baseLayout.addView(navButtonView)

        return baseLayout
    }


    /**
     * Get the navigation items of the page
     * @return The navigation items of the page
     */
    private fun createNavButtonView(): LinearLayout {
        val navButtonView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
            // Purple background
            background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.menu_nav_background,
                null
            )
            setPadding(20, 20, 20, 20)
        }
        return navButtonView
    }


    /**
     * Create a row layout
     * @return The row layout
     */
    private fun createRowLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
            gravity = Gravity.CENTER
        }
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