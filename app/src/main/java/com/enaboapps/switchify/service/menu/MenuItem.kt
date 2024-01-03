package com.enaboapps.switchify.service.menu

import android.widget.Button
import android.widget.LinearLayout

// This is a base class for all menu items
// Text - The text to display for the menu item
// Action - The action to perform when the menu item is selected
// Highlighted - Whether or not the menu item should be highlighted (being scanned by switch access)
class MenuItem(val text: String, val action: () -> Unit, var highlighted: Boolean = false) {
    // button is the button that is displayed for the menu item
    private var button: Button? = null
    // Inflate the menu item
    fun inflate(linearLayout: LinearLayout) {
        button = Button(linearLayout.context)
        button?.text = text
        button?.setOnClickListener {
            select()
        }
        linearLayout.addView(button)
    }

    // This function is called when the menu item is selected
    fun select() {
        action()
    }

    // This function is called when the menu item is highlighted
    fun highlight() {
        button?.context?.resources?.getColor(android.R.color.holo_blue_light, null)?.let {
            button?.setBackgroundColor(it)
        }
        highlighted = true
    }

    // This function is called when the menu item is unhighlighted
    fun unhighlight() {
        button?.context?.resources?.getColor(android.R.color.white, null)?.let {
            button?.setBackgroundColor(it)
        }
        highlighted = false
    }
}