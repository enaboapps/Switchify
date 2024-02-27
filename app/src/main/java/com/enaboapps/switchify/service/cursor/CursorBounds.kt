package com.enaboapps.switchify.service.cursor

import android.content.Context
import com.enaboapps.switchify.service.utils.KeyboardInfo
import com.enaboapps.switchify.service.utils.ScreenUtils

object CursorBounds {
    const val X_MIN = 0

    /**
     * Y min cursor bounds
     * @param context The context
     * @return The minimum y value for the cursor based on the keyboard visibility
     */
    fun yMin(context: Context): Int {
        return if (KeyboardInfo.isKeyboardVisible) {
            ScreenUtils.getHeight(context) - KeyboardInfo.keyboardHeight
        } else {
            0
        }
    }

    /**
     * X max cursor bounds
     * @param context The context
     * @return The maximum x value for the cursor
     */
    fun xMax(context: Context): Int {
        return ScreenUtils.getWidth(context)
    }

    /**
     * Y max cursor bounds
     * @param context The context
     * @return The maximum y value for the cursor
     */
    fun yMax(context: Context): Int {
        return ScreenUtils.getHeight(context)
    }

    /**
     * Width cursor bounds
     * @param context The context
     * @return The width of the cursor bounds
     */
    fun width(context: Context): Int {
        return xMax(context)
    }

    /**
     * Height cursor bounds
     * @param context The context
     * @return The height of the cursor bounds
     */
    fun height(context: Context): Int {
        return yMax(context) - yMin(context)
    }
}