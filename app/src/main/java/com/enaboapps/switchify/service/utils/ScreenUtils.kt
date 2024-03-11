package com.enaboapps.switchify.service.utils

import android.content.Context
import android.view.WindowMetrics

/**
 * This class provides utility functions to get screen dimensions.
 */
class ScreenUtils {

    companion object {
        /**
         * Returns the width of the screen in pixels.
         *
         * @param context The context of the caller.
         * @return The width of the screen in pixels.
         */
        fun getWidth(context: Context): Int {
            return getWindowMetrics(context).bounds.width()
        }

        /**
         * Returns the height of the screen in pixels.
         *
         * @param context The context of the caller.
         * @return The height of the screen in pixels.
         */
        fun getHeight(context: Context): Int {
            return getWindowMetrics(context).bounds.height()
        }

        /**
         * Converts dp to pixels.
         *
         * @param context The context of the caller.
         * @param dp The value in dp to convert.
         */
        fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        /**
         * Retrieves the current window metrics.
         *
         * @param context The context of the caller.
         * @return The current window metrics.
         */
        private fun getWindowMetrics(context: Context): WindowMetrics {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
            return windowManager.currentWindowMetrics
        }
    }

}