package com.enaboapps.switchify.keyboard

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button

class KeyboardKey(context: Context) : Button(context) {

    var keyType: KeyType? = null
        set(value) {
            field = value
            text = value.toString() // Set the button text to the key's label
        }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo?) {
        super.onInitializeAccessibilityNodeInfo(info)
        // Customize accessibility node info as needed
        info?.className = Button::class.java.name
        info?.contentDescription = keyType.toString()
        // Depending on the keyType, you may want to add more descriptive content or actions
    }
}
