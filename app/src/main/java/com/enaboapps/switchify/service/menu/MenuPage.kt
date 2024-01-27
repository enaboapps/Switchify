package com.enaboapps.switchify.service.menu

import android.content.Context
import android.widget.LinearLayout

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
            var menuChangeBtnText = "Next"
            if (pageIndex == maxPageIndex) {
                menuChangeBtnText = "Previous"
            }
            menuChangeBtn = MenuItem(menuChangeBtnText, false) {
                changePage()
            }
            _menuItems += menuChangeBtn!!
        }
    }


    fun getMenuItems(): List<MenuItem> {
        return _menuItems
    }


    fun getMenuLayout(): LinearLayout {
        linearLayout.removeAllViews()
        _menuItems.forEach {
            it.inflate(linearLayout)
        }
        return linearLayout
    }


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