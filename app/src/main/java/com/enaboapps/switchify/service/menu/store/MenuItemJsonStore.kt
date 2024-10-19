package com.enaboapps.switchify.service.menu.store

import android.content.Context
import com.enaboapps.switchify.service.menu.MenuItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MenuItemJsonStore(private val context: Context) {
    private val gson = Gson()
    private val fileName = "menu_items.json"
    private val file = File(context.filesDir, fileName)

    init {
        if (!file.exists()) {
            file.createNewFile()
            file.writeText("[]")
        }
    }

    fun addMenuItem(menuItem: MenuItem) {
        val menuItems = getMenuItems().toMutableList()
        menuItems.add(menuItem)
        saveMenuItems(menuItems)
    }

    fun removeMenuItem(menuItem: MenuItem) {
        val menuItems = getMenuItems().toMutableList()
        menuItems.remove(menuItem)
        saveMenuItems(menuItems)
    }

    fun getMenuItems(): List<MenuItem> {
        val json = file.readText()
        val type = object : TypeToken<List<MenuItem>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveMenuItems(menuItems: List<MenuItem>) {
        val json = gson.toJson(menuItems)
        file.writeText(json)
    }
}
