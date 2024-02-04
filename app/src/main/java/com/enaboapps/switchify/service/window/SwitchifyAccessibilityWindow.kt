package com.enaboapps.switchify.service.window

import android.content.Context
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout

class SwitchifyAccessibilityWindow {

    private val TAG = "SwitchifyAccessibilityWindow"


    private var windowManager: WindowManager? = null
    private var baseLayout: RelativeLayout? = null


    companion object {
        val instance: SwitchifyAccessibilityWindow by lazy {
            SwitchifyAccessibilityWindow()
        }
    }


    fun setup(context: Context) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        baseLayout = RelativeLayout(context)
        ServiceMessageHUD.instance.setup(context)
    }

    fun show() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        windowManager?.addView(baseLayout, params)
    }

    // This function adds a view to the window with the given x, y, width, and height
    fun addView(view: ViewGroup, x: Int, y: Int, width: Int, height: Int) {
        val params = RelativeLayout.LayoutParams(width, height)
        params.leftMargin = x
        params.topMargin = y
        baseLayout?.addView(view, params)
    }

    // This function adds a view to the center of the window
    fun addViewToCenter(view: ViewGroup) {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        baseLayout?.addView(view, params)
    }

    fun updateViewLayout(view: ViewGroup, x: Int, y: Int) {
        val params = view.layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = x
        params.topMargin = y
        try {
            baseLayout?.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeView(view: ViewGroup) {
        try {
            baseLayout?.removeView(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}