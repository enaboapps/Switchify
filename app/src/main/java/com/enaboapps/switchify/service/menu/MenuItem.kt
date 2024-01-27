package com.enaboapps.switchify.service.menu

import android.util.Log
import android.widget.Button
import android.widget.LinearLayout

// This is a base class for all menu items
// Text - The text to display for the menu item
// Close on select - Whether or not the menu should be closed when the menu item is selected
// Is menu nav item - Whether or not the menu item is a menu navigation item
// Page - The page that the menu item is on
// Action - The action to perform when the menu item is selected
class MenuItem {
    private val text: String
    val closeOnSelect: Boolean
    private var isMenuNavItem: Boolean = false
    var page: Int = 0
    private val action: () -> Unit

    // highlighted is whether or not the menu item is highlighted
    private var highlighted = false

    // button is the button that is displayed for the menu item
    private var button: Button? = null

    constructor(text: String, closeOnSelect: Boolean = true, action: () -> Unit) {
        this.text = text
        this.closeOnSelect = closeOnSelect
        this.action = action
    }

    constructor(
        text: String,
        closeOnSelect: Boolean = true,
        isMenuNavItem: Boolean = false,
        action: () -> Unit
    ) {
        this.text = text
        this.closeOnSelect = closeOnSelect
        this.isMenuNavItem = isMenuNavItem
        this.action = action
    }

    // Inflate the menu item
    fun inflate(linearLayout: LinearLayout) {
        button = Button(linearLayout.context)
        button?.text = text
        button?.setBackgroundColor(
            linearLayout.context.resources.getColor(
                android.R.color.black,
                null
            )
        )
        button?.setTextColor(linearLayout.context.resources.getColor(android.R.color.white, null))
        // padding
        button?.setPadding(30, 30, 30, 30)
        button?.setOnClickListener {
            select()
        }
        linearLayout.addView(button)
    }

    // This function is called when the menu item is selected
    fun select() {
        Log.d("MenuItem", "Selected menu item: $text")
        if (!isMenuNavItem && closeOnSelect) {
            MenuManager.getInstance().closeMenuHierarchy()
        }
        action()
    }

    // This function is called when the menu item is highlighted
    fun highlight() {
        try {
            button?.context?.resources?.getColor(android.R.color.white, null)?.let {
                button?.setBackgroundColor(it)
            }
            button?.context?.resources?.getColor(android.R.color.black, null)?.let {
                button?.setTextColor(it)
            }
            highlighted = true
        } catch (e: Exception) {
            Log.e("MenuItem", "Error highlighting menu item", e)
        }
    }

    // This function is called when the menu item is unhighlighted
    fun unhighlight() {
        try {
            button?.context?.resources?.getColor(android.R.color.black, null)?.let {
                button?.setBackgroundColor(it)
            }
            button?.context?.resources?.getColor(android.R.color.white, null)?.let {
                button?.setTextColor(it)
            }
            highlighted = false
        } catch (e: Exception) {
            Log.e("MenuItem", "Error unhighlighting menu item", e)
        }
    }
}