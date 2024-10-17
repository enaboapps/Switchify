package com.enaboapps.switchify.service.window

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout

class SwitchifyAccessibilityWindow {

    private var windowManager: WindowManager? = null
    private var baseLayout: RelativeLayout? = null
    private var context: Context? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "SwitchifyAccessibilityWindow"
        val instance: SwitchifyAccessibilityWindow by lazy {
            SwitchifyAccessibilityWindow()
        }
    }

    fun setup(context: Context) {
        try {
            this.context = context
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            baseLayout = RelativeLayout(context)
            ServiceMessageHUD.instance.setup(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error in setup: ${e.message}", e)
        }
    }

    fun getContext(): Context? {
        return context
    }

    fun show() {
        mainHandler.post {
            try {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                )
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

                windowManager?.addView(baseLayout, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in show: ${e.message}", e)
            }
        }
    }

    fun addView(view: ViewGroup, x: Int, y: Int, width: Int, height: Int) {
        mainHandler.post {
            try {
                val params = RelativeLayout.LayoutParams(width, height)
                params.leftMargin = x
                params.topMargin = y
                baseLayout?.addView(view, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in addView: ${e.message}", e)
            }
        }
    }

    fun addView(view: ViewGroup, x: Int, y: Int) {
        mainHandler.post {
            try {
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                params.leftMargin = x
                params.topMargin = y
                baseLayout?.addView(view, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in addView: ${e.message}", e)
            }
        }
    }

    fun addViewToBottom(view: ViewGroup, margins: Int = 0) {
        mainHandler.post {
            try {
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                params.addRule(RelativeLayout.CENTER_HORIZONTAL)
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                params.setMargins(margins, margins, margins, margins)
                baseLayout?.addView(view, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in addView: ${e.message}", e)
            }
        }
    }

    fun addViewToCenter(view: ViewGroup) {
        mainHandler.post {
            try {
                val params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                params.addRule(RelativeLayout.CENTER_IN_PARENT)
                baseLayout?.addView(view, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in addViewToCenter: ${e.message}", e)
            }
        }
    }

    fun updateViewLayout(view: ViewGroup, x: Int, y: Int) {
        mainHandler.post {
            try {
                val params = view.layoutParams as RelativeLayout.LayoutParams
                params.leftMargin = x
                params.topMargin = y

                // Ensure the view stays within screen bounds
                val screenWidth = context?.resources?.displayMetrics?.widthPixels ?: 0
                val screenHeight = context?.resources?.displayMetrics?.heightPixels ?: 0

                if (params.leftMargin + view.width > screenWidth) {
                    params.leftMargin = screenWidth - view.width
                }
                if (params.topMargin + view.height > screenHeight) {
                    params.topMargin = screenHeight - view.height
                }

                baseLayout?.updateViewLayout(view, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateViewLayout: ${e.message}", e)
            }
        }
    }

    fun updateViewLayout(view: ViewGroup, x: Int, y: Int, width: Int, height: Int) {
        mainHandler.post {
            try {
                val params = view.layoutParams as RelativeLayout.LayoutParams
                params.leftMargin = x
                params.topMargin = y
                params.width = width
                params.height = height

                // Ensure the view stays within screen bounds
                val screenWidth = context?.resources?.displayMetrics?.widthPixels ?: 0
                val screenHeight = context?.resources?.displayMetrics?.heightPixels ?: 0

                if (params.leftMargin + params.width > screenWidth) {
                    params.leftMargin = screenWidth - params.width
                }
                if (params.topMargin + params.height > screenHeight) {
                    params.topMargin = screenHeight - params.height
                }

                baseLayout?.updateViewLayout(view, params)
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateViewLayout: ${e.message}", e)
            }
        }
    }

    fun removeView(view: ViewGroup) {
        mainHandler.post {
            try {
                baseLayout?.removeView(view)
            } catch (e: Exception) {
                Log.e(TAG, "Error in removeView: ${e.message}", e)
            }
        }
    }
}
