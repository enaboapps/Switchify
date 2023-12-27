package com.enaboapps.switchify.service

import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.ContextCompat.getSystemService

// This is a class that is not used in the app, but is used in the tests.
// It is used to test the accessibility service.
// It creates a button that can be clicked to simulate a switch event.

// This is the interface that is used to listen for taps.
interface ScreenSwitchListener {
    fun onScreenSwitch()
}

class ScreenSwitch(private val context: Context) {
    var screenSwitchListener: ScreenSwitchListener? = null

    private var windowManager: WindowManager? = null

    private var button: Button? = null
    private var buttonLayoutParams: WindowManager.LayoutParams? = null



    // Set up the button at the bottom of the screen.
    fun setup() {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Getting screen dimensions
        val displayMetrics = context.resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        // Setting up LayoutParams
        buttonLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        // Adjusting the position to the bottom of the screen
        buttonLayoutParams?.gravity = Gravity.BOTTOM
        buttonLayoutParams?.x = 0
        buttonLayoutParams?.y = 0 // Y is now set to 0 with gravity at the bottom

        button = Button(context)
        button?.setOnClickListener {
            performAction()
        }

        windowManager?.addView(button, buttonLayoutParams)
    }

    // Function to remove the button from the screen.
    fun teardown() {
        windowManager?.removeView(button)
    }

    fun performAction() {
        screenSwitchListener?.onScreenSwitch()
    }
}