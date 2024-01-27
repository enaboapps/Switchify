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

    private fun canPopMenu(): Boolean {
        return tree.size > 1
    }

    fun popMenu() {
        if (canPopMenu()) {
            tree.lastOrNull()?.close()
            tree = tree.dropLast(1)
            Handler(Looper.getMainLooper()).postDelayed(100) {
                tree.lastOrNull()?.let {
                    it.menuViewListener = this
                    it.open(scanningManager)
                }
            }
        }
    }

    fun openMenu(menu: MenuView) {
        getTopMenu()?.close()

        addMenu(menu)
        menu.menuViewListener = this
        Handler(Looper.getMainLooper()).postDelayed(100) {
            menu.open(scanningManager)
        }
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