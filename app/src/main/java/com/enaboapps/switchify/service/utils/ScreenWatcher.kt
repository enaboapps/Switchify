package com.enaboapps.switchify.service.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * This class watches the screen for changes.
 */
class ScreenWatcher(
    private val onScreenWake: (() -> Unit)? = null,
    private val onScreenSleep: (() -> Unit)? = null
) {

    private var isScreenOn = true

    /**
     * The broadcast receiver for screen changes.
     */
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> onScreenWake()
                Intent.ACTION_SCREEN_OFF -> onScreenSleep()
            }
        }
    }

    fun register(context: Context) {
        context.registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
    }

    /**
     * This method is called when the screen is turned on.
     */
    fun onScreenWake() {
        if (!isScreenOn) {
            isScreenOn = true
            onScreenWake?.invoke()
        }
    }

    /**
     * This method is called when the screen is turned off.
     */
    fun onScreenSleep() {
        if (isScreenOn) {
            isScreenOn = false
            onScreenSleep?.invoke()
        }
    }
}