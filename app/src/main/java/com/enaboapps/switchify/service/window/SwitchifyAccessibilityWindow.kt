package com.enaboapps.switchify.service.window

import android.content.Context
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout

class SwitchifyAccessibilityWindow(private val context: Context) {

    private val TAG = "SwitchifyAccessibilityWindow"

    private var windowManager: WindowManager? = null
    private var baseLayout: RelativeLayout? = null

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        baseLayout = RelativeLayout(context)

        ServiceMessageHUD.instance.setup(this)
    }

    fun getContext(): Context {
        return context
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

    fun addView(view: ViewGroup, x: Int, y: Int, width: Int, height: Int) {
        val params = RelativeLayout.LayoutParams(width, height)
        params.leftMargin = x
        params.topMargin = y
        baseLayout?.addView(view, params)
    }

    fun updateViewLayout(view: ViewGroup, x: Int, y: Int) {
        val params = view.layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = x
        params.topMargin = y
        baseLayout?.updateViewLayout(view, params)
    }

    fun removeView(view: ViewGroup) {
        baseLayout?.removeView(view)
    }
}