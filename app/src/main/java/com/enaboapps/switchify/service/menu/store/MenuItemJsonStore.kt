package com.enaboapps.switchify.service.menu.store

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.service.custom.actions.data.ACTION_OPEN_APP
import com.enaboapps.switchify.service.custom.actions.data.ActionExtra
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Represents a menu item with a unique identifier, action, text, and optional extra data.
 *
 * @property id A unique identifier for the menu item, generated as a UUID string.
 * @property action The action associated with this menu item.
 * @property text The text to be displayed for this menu item.
 * @property extra Optional additional data associated with the menu item.
 */
data class MenuItemJson(
    @SerializedName("id") val id: String = UUID.randomUUID().toString(),
    @SerializedName("action") val action: String,
    @SerializedName("text") val text: String,
    @SerializedName("extra") val extra: ActionExtra? = null
) {
    companion object {
        /**
         * Creates a MenuItemJson object from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return A MenuItemJson object, or null if parsing fails.
         */
        fun fromJson(json: String): MenuItemJson? = try {
            Gson().fromJson(json, MenuItemJson::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("MenuItemJson", "Error parsing JSON: ${e.message}")
            null
        }
    }

    /**
     * Converts the MenuItemJson object to a JSON string.
     *
     * @return A JSON string representation of the object.
     */
    fun toJson(): String = Gson().toJson(this)
}

/**
 * Manages the storage and retrieval of menu items using JSON serialization.
 *
 * @property context The Android application context.
 */
class MenuItemJsonStore(private val context: Context) {
    private val gson = Gson()
    private val fileName = "menu_items.json"
    private val file = File(context.filesDir, fileName)
    private val tag = "MenuItemJsonStore"

    // In-memory cache of menu items
    private var items: MutableList<MenuItemJson> = mutableListOf()

    init {
        try {
            // Ensure the file exists and load items
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("[]")
            }
            loadItems()
        } catch (e: IOException) {
            Log.e(tag, "Error initializing file: ${e.message}")
        }
    }

    /**
     * Loads menu items from the JSON file into memory.
     */
    private fun loadItems() {
        try {
            val json = file.readText()
            val type = object : TypeToken<List<MenuItemJson>>() {}.type
            items =
                gson.fromJson<List<MenuItemJson>>(json, type)?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            Log.e(tag, "Error loading menu items: ${e.message}")
            items = mutableListOf()
        }
    }

    /**
     * Checks if the menu item store is empty.
     *
     * @return True if the menu item store is empty, false otherwise.
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Returns a list of available actions.
     *
     * @return A list of strings representing available actions.
     */
    fun getAvailableActions(): List<String> = listOf(ACTION_OPEN_APP)

    /**
     * Adds a new menu item.
     *
     * @param action The action for the new menu item.
     * @param text The text to be displayed for the new menu item.
     * @param extra Optional extra data for the menu item.
     * @return The ID of the newly created menu item, or an empty string if an error occurred.
     */
    fun addMenuItem(action: String, text: String, extra: ActionExtra? = null): String {
        try {
            val menuItemJson = MenuItemJson(action = action, text = text, extra = extra)
            items.add(menuItemJson)
            saveMenuItems()
            return menuItemJson.id
        } catch (e: Exception) {
            Log.e(tag, "Error adding menu item: ${e.message}")
            return ""
        }
    }

    /**
     * Removes a menu item by its ID.
     *
     * @param id The ID of the menu item to remove.
     */
    fun removeMenuItem(id: String) {
        try {
            items.removeIf { it.id == id }
            saveMenuItems()
        } catch (e: Exception) {
            Log.e(tag, "Error removing menu item: ${e.message}")
        }
    }

    /**
     * Updates an existing menu item.
     *
     * @param id The ID of the menu item to update.
     * @param action The new action for the menu item (optional).
     * @param text The new text for the menu item (optional).
     * @param extra The new extra data for the menu item (optional).
     */
    fun updateMenuItem(
        id: String,
        action: String? = null,
        text: String? = null,
        extra: ActionExtra? = null
    ) {
        try {
            val index = items.indexOfFirst { it.id == id }
            if (index != -1) {
                val currentItem = items[index]
                items[index] = currentItem.copy(
                    action = action ?: currentItem.action,
                    text = text ?: currentItem.text,
                    extra = extra ?: currentItem.extra
                )
                saveMenuItems()
            } else {
                Log.w(tag, "Menu item not found for update: $id")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating menu item: ${e.message}")
        }
    }

    /**
     * Retrieves all menu items.
     *
     * @return A list of all MenuItemJson objects.
     */
    fun getMenuItems(): List<MenuItemJson> = items.toList()

    /**
     * Saves the current list of menu items to the JSON file.
     */
    private fun saveMenuItems() {
        try {
            val json = gson.toJson(items)
            file.writeText(json)
        } catch (e: Exception) {
            Log.e(tag, "Error saving menu items: ${e.message}")
        }
    }

    /**
     * Retrieves a menu item by its ID.
     *
     * @param id The ID of the menu item to retrieve.
     * @return The MenuItemJson object if found, null otherwise.
     */
    fun getMenuItem(id: String): MenuItemJson? {
        return try {
            items.find { it.id == id }
        } catch (e: Exception) {
            Log.e(tag, "Error getting menu item: ${e.message}")
            null
        }
    }

    /**
     * Retrieves a menu item by its action.
     *
     * @param action The action of the menu item to retrieve.
     * @return The MenuItemJson object if found, null otherwise.
     */
    fun getMenuItemByAction(action: String): MenuItemJson? {
        return try {
            items.find { it.action == action }
        } catch (e: Exception) {
            Log.e(tag, "Error getting menu item by action: ${e.message}")
            null
        }
    }
}