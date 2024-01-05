package com.enaboapps.switchify.service.window

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import java.lang.ref.WeakReference

class SwitchifyHUD private constructor(context: Context) {

    private val TAG = "SwitchifyHUD"

    private var windowManager: WindowManager? = null
    private var baseLayout: FrameLayout? = null

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        baseLayout = FrameLayout(context)
    }

    fun show() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager?.addView(baseLayout, params)
    }

    fun addView(view: LinearLayout, x: Int, y: Int, width: Int, height: Int) {
        val params = FrameLayout.LayoutParams(width, height)
        params.gravity = Gravity.TOP or Gravity.START
        params.leftMargin = x
        params.topMargin = y
        baseLayout?.addView(view, params)
    }

    fun updateViewLayout(view: LinearLayout, x: Int, y: Int) {
        val params = FrameLayout.LayoutParams(view.width, view.height)
        params.leftMargin = x
        params.topMargin = y
        baseLayout?.updateViewLayout(view, params)
    }

    fun removeView(view: LinearLayout) {
        baseLayout?.removeView(view)
    }

    companion object {
        @Volatile
        private var INSTANCE: WeakReference<SwitchifyHUD>? = null

        fun getInstance(context: Context): SwitchifyHUD {
            return INSTANCE?.get() ?: synchronized(this) {
                INSTANCE?.get() ?: SwitchifyHUD(context).also {
                    INSTANCE = WeakReference(it)
                }
            }
        }
    }
}