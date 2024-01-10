package com.enaboapps.switchify.service.menu

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import com.enaboapps.switchify.service.scanning.ScanningManager

class MenuHierarchy(
    private val scanningManager: ScanningManager
) : MenuViewListener {
    private val TAG = "SwitchifyMenuHierarchy"

    private var tree: List<MenuView> = mutableListOf()

    private fun addMenu(menu: MenuView) {
        tree += menu
    }

    fun canPopMenu(): Boolean {
        return tree.size > 1
    }

    fun popMenu() {
        if (canPopMenu()) {
            getTopMenu()?.close()
            tree = tree.dropLast(1)
            tree.last().menuViewListener = this
            Handler(Looper.getMainLooper()).postDelayed(100) {
                scanningManager.setMenuState()
                tree.last().open()
            }
        }
    }

    fun openMenu(menu: MenuView) {
        addMenu(menu)
        if (tree.size > 1) {
            tree[tree.size - 2].close()
        }
        menu.menuViewListener = this
        Handler(Looper.getMainLooper()).postDelayed(100) {
            scanningManager.setMenuState()
            menu.open()
        }

        // set the state to menu
        scanningManager.setMenuState()
    }

    fun removeAllMenus() {
        // close the top menu
        getTopMenu()?.close()
        tree = mutableListOf()

        // set the state to cursor
        scanningManager.setCursorState()
    }

    fun getTopMenu(): MenuView? {
        return tree.lastOrNull()
    }


    override fun onMenuViewClosed() {
        scanningManager.setCursorState()
    }
}