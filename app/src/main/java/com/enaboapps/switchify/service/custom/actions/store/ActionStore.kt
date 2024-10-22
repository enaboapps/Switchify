package com.enaboapps.switchify.service.custom.actions.store

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.service.custom.actions.store.data.ACTIONS
import com.enaboapps.switchify.service.custom.actions.store.data.ActionExtra
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Action represents a custom action that can be performed by the user.
 *
 * @property id A unique identifier for the action, generated as a UUID string.
 * @property action The action to be performed.
 * @property text The text to be displayed for the action.
 * @property extra Optional extra data for the action.
 */
data class Action(
    @SerializedName("id") val id: String = UUID.randomUUID().toString(),
    @SerializedName("action") val action: String,
    @SerializedName("text") val text: String,
    @SerializedName("extra") val extra: ActionExtra? = null
) {
    companion object {
        /**
         * Creates an Action object from a JSON string.
         *
         * @param json The JSON string to parse.
         * @return An Action object, or null if parsing fails.
         */
        fun fromJson(json: String): Action? = try {
            Gson().fromJson(json, Action::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("Action", "Error parsing JSON: ${e.message}")
            null
        }
    }

    /**
     * Converts the Action object to a JSON string.
     *
     * @return A JSON string representation of the object.
     */
    fun toJson(): String = Gson().toJson(this)
}

/**
 * Manages the storage and retrieval of actions using JSON serialization.
 *
 * @property context The Android application context.
 */
class ActionStore(private val context: Context) {
    private val gson = Gson()
    private val fileName = "actions.json"
    private val file = File(context.filesDir, fileName)
    private val tag = "ActionStore"

    // In-memory cache of actions
    private var items: MutableList<Action> = mutableListOf()

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
     * Loads actions from the JSON file into memory.
     */
    private fun loadItems() {
        try {
            val json = file.readText()
            val type = object : TypeToken<List<Action>>() {}.type
            items =
                gson.fromJson<List<Action>>(json, type)?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            Log.e(tag, "Error loading actions: ${e.message}")
            items = mutableListOf()
        }
    }

    /**
     * Checks if the action store is empty.
     *
     * @return True if the action store is empty, false otherwise.
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Returns a list of available actions.
     *
     * @return A list of strings representing available actions.
     */
    fun getAvailableActions(): List<String> = ACTIONS

    /**
     * Adds a new action.
     *
     * @param action The action for the new action.
     * @param text The text to be displayed for the new action.
     * @param extra Optional extra data for the action.
     * @return The ID of the newly created action, or an empty string if an error occurred.
     */
    fun addAction(action: String, text: String, extra: ActionExtra? = null): String {
        try {
            val actionJson = Action(action = action, text = text, extra = extra)
            items.add(actionJson)
            saveActions()
            return actionJson.id
        } catch (e: Exception) {
            Log.e(tag, "Error adding action: ${e.message}")
            return ""
        }
    }

    /**
     * Removes an action by its ID.
     *
     * @param id The ID of the action to remove.
     */
    fun removeAction(id: String) {
        try {
            items.removeIf { it.id == id }
            saveActions()
        } catch (e: Exception) {
            Log.e(tag, "Error removing action: ${e.message}")
        }
    }

    /**
     * Updates an existing action.
     *
     * @param id The ID of the action to update.
     * @param action The new action for the action (optional).
     * @param text The new text for the action (optional).
     * @param extra The new extra data for the action (optional).
     */
    fun updateAction(
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
                saveActions()
            } else {
                Log.w(tag, "Action not found for update: $id")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating action: ${e.message}")
        }
    }

    /**
     * Retrieves all actions.
     *
     * @return A list of all Action objects.
     */
    fun getActions(): List<Action> = items.toList()

    /**
     * Saves the current list of actions to the JSON file.
     */
    private fun saveActions() {
        try {
            val json = gson.toJson(items)
            file.writeText(json)
        } catch (e: Exception) {
            Log.e(tag, "Error saving actions: ${e.message}")
        }
    }

    /**
     * Retrieves an action by its ID.
     *
     * @param id The ID of the action to retrieve.
     * @return The Action object if found, null otherwise.
     */
    fun getAction(id: String): Action? {
        return try {
            items.find { it.id == id }
        } catch (e: Exception) {
            Log.e(tag, "Error getting action: ${e.message}")
            null
        }
    }

    /**
     * Retrieves an action by its action.
     *
     * @param action The action of the action to retrieve.
     * @return The Action object if found, null otherwise.
     */
    fun getActionByAction(action: String): Action? {
        return try {
            items.find { it.action == action }
        } catch (e: Exception) {
            Log.e(tag, "Error getting action by action: ${e.message}")
            null
        }
    }
}
