package com.enaboapps.switchify.service.utils

import android.content.Context
import android.view.WindowMetrics

class ScreenUtils {

    companion object {
        fun getWidth(context: Context): Int {
            return getWindowMetrics(context).bounds.width()
        }

        fun getHeight(context: Context): Int {
            return getWindowMetrics(context).bounds.height()
        }

        private fun getWindowMetrics(context: Context): WindowMetrics {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            return windowManager.currentWindowMetrics
        }
    }

}