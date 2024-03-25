package com.enaboapps.switchify.keyboard

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson

data class KeyInfo(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class KeyboardLayoutInfo(
    val keys: List<KeyInfo>
)

class KeyboardAccessibilityManager(private val context: Context) {

    companion object {
        const val ACTION_KEYBOARD_LAYOUT_INFO = "com.enaboapps.ACTION_KEYBOARD_LAYOUT_INFO"
        const val EXTRA_KEYBOARD_LAYOUT_INFO = "keyboardLayoutInfo"
    }

    fun captureAndBroadcastLayoutInfo(keyboardView: ViewGroup) {
        val layoutInfo = captureKeyboardLayoutInfo(keyboardView)
        broadcastKeyboardLayoutInfo(layoutInfo)
    }

    private fun captureKeyboardLayoutInfo(keyboardView: ViewGroup): KeyboardLayoutInfo {
        val keyInfos = mutableListOf<KeyInfo>()
        val intArray = IntArray(2)

        for (i in 0 until keyboardView.childCount) {
            val child = keyboardView.getChildAt(i)
            if (child !is KeyboardKey && child is ViewGroup) {
                // Recursive call for nested ViewGroup
                keyInfos.addAll(captureKeyboardLayoutInfo(child).keys)
            } else if (child is KeyboardKey) {
                child.getLocationOnScreen(intArray)
                keyInfos.add(
                    KeyInfo(
                        x = intArray[0],
                        y = intArray[1],
                        width = child.width,
                        height = child.height
                    )
                )
            }
        }

        return KeyboardLayoutInfo(keys = keyInfos)
    }

    private fun broadcastKeyboardLayoutInfo(layoutInfo: KeyboardLayoutInfo) {
        val jsonLayoutInfo = Gson().toJson(layoutInfo)
        val intent = Intent(ACTION_KEYBOARD_LAYOUT_INFO).apply {
            putExtra(EXTRA_KEYBOARD_LAYOUT_INFO, jsonLayoutInfo)
        }
        println(jsonLayoutInfo)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

}