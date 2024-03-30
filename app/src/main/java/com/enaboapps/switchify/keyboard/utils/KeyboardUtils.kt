package com.enaboapps.switchify.keyboard.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

object KeyboardUtils {
    /**
     * Function to check if Switchify keyboard is enabled
     *
     * @param context the context
     * @return true if Switchify keyboard is enabled, false otherwise
     */
    fun isSwitchifyKeyboardEnabled(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val list = imm.enabledInputMethodList
        for (inputMethodInfo in list) {
            if (inputMethodInfo.packageName == context.packageName) {
                return true
            }
        }
        return false
    }

    /**
     * Function to open the input method settings
     *
     * @param context the context
     */
    fun openInputMethodSettings(context: Context) {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        context.startActivity(intent)
    }
}