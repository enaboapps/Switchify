package com.enaboapps.switchify.service.menu

import android.content.Context
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import com.enaboapps.switchify.preferences.PreferenceManager
import java.util.Timer
import java.util.TimerTask

interface MenuViewListener {
    fun onMenuViewClosed()
}

class MenuView(val context: Context, var menuItems: MutableList<MenuItem>) {

    var menuViewListener: MenuViewListener? = null

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var linearLayout = LinearLayout(context)

    // scanIndex is the index of the menu item that is currently being scanned
    var scanIndex = 0
    // timer is the timer that is used to scan the menu items
    private var timer: Timer? = null


    // This function is called when the menu is opened
    fun open() {
        // Create a LinearLayout
        linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        // White background
        linearLayout.setBackgroundColor(0xFFFFFFFF.toInt())
        // Add a close menu item
        val closeMenuItem = MenuItem("Close Menu", {
            close()
        })
        menuItems.add(closeMenuItem)
        // Iterate through the menu items and inflate them
        for (menuItem in menuItems) {
            menuItem.inflate(linearLayout)
        }
        // Add the LinearLayout to the WindowManager
        windowManager.addView(linearLayout, WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            0
        ))
    }

    // This function is called when the menu is closed
    fun close() {
        // Remove the LinearLayout from the WindowManager
        windowManager.removeView(linearLayout)
        // Call the listener
        menuViewListener?.onMenuViewClosed()
    }

    // This function starts scanning the menu items
    fun startScanning() {
        // Set the first menu item to be highlighted
        menuItems[scanIndex].highlight()
        val rate = PreferenceManager(context).getIntegerValue(PreferenceManager.Keys.PREFERENCE_KEY_SCAN_RATE)
        // Start the timer
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Unhighlight the current menu item
                menuItems[scanIndex].unhighlight()
                // Increment the scan index
                scanIndex++
                // If the scan index is greater than the number of menu items, reset it to 0
                if (scanIndex >= menuItems.size) {
                    scanIndex = 0
                }
                // Highlight the current menu item
                menuItems[scanIndex].highlight()

                Log.d("MenuView", "Scanning menu item ${scanIndex}")
            }
        }, rate.toLong(), rate.toLong())
    }

    // This function stops scanning the menu items
    fun stopScanning() {
        // Cancel the timer
        timer?.cancel()
        // Unhighlight the current menu item
        menuItems[scanIndex].unhighlight()
        // Reset the scan index to 0
        scanIndex = 0
    }

    // This function returns whether or not the menu is scanning
    fun isScanning(): Boolean {
        return timer != null
    }

    // This function either starts scanning or selects the current menu item
    fun select() {
        Log.d("MenuView", "Selecting menu item ${scanIndex}")
        if (isScanning()) {
            menuItems[scanIndex].select()
            stopScanning()
            close()
        } else {
            startScanning()
        }
    }
}