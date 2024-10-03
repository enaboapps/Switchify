package com.enaboapps.switchify.switches

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanMode
import java.io.File

class SwitchEventStore(private val context: Context) {
    private val switchEvents = mutableSetOf<SwitchEvent>()
    private val fileName = "switch_events.txt"
    private val file: File
        get() = File(context.applicationContext.filesDir, fileName)

    init {
        readFile()
    }

    fun add(switchEvent: SwitchEvent) {
        if (switchEvents.add(switchEvent)) {
            saveToFile()
        }
    }

    fun update(switchEvent: SwitchEvent) {
        if (file.exists()) {
            try {
                val newLines = file.readLines().map { line ->
                    val parts = line.split(", ")
                    if (parts[1] == switchEvent.code) switchEvent.toString() else line
                }
                file.writeText(newLines.joinToString("\n"))
                readFile() // Refresh the in-memory set
            } catch (e: Exception) {
                Log.e("SwitchEventStore", "Error updating file", e)
            }
        } else {
            Log.d("SwitchEventStore", "File does not exist")
        }
    }

    fun remove(switchEvent: SwitchEvent) {
        if (switchEvents.remove(switchEvent)) {
            saveToFile()
        }
    }

    fun find(code: String): SwitchEvent? =
        switchEvents.find { it.code == code }?.also {
            Log.d("SwitchEventStore", "Found switch event for code $code")
        } ?: run {
            Log.d("SwitchEventStore", "No switch event found for code $code")
            null
        }

    fun getCount(): Int = switchEvents.size

    fun getSwitchEvents(): Set<SwitchEvent> = switchEvents.toSet()

    private fun readFile() {
        if (file.exists()) {
            try {
                switchEvents.clear()
                file.readLines()
                    .filter { it.isNotBlank() }
                    .forEach { line ->
                        try {
                            val switchEvent = SwitchEvent.fromString(line)
                            switchEvents.add(switchEvent)
                        } catch (e: Exception) {
                            Log.e("SwitchEventStore", "Error parsing switch event: $line", e)
                        }
                    }
            } catch (e: Exception) {
                Log.e("SwitchEventStore", "Error reading from file", e)
                deleteFile()
                Toast.makeText(
                    context,
                    "Error reading from file. Please reconfigure your switches.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d("SwitchEventStore", "File does not exist")
        }
    }

    private fun deleteFile() {
        if (file.exists()) {
            file.delete()
        }
    }

    private fun saveToFile() {
        try {
            file.writeText(switchEvents.joinToString("\n") { it.toString() })
        } catch (e: Exception) {
            Log.e("SwitchEventStore", "Error writing to file", e)
        }
    }

    fun isConfigInvalid(): String? {
        val preferenceManager = PreferenceManager(context)
        val mode =
            ScanMode(preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_SCAN_MODE))
        val containsSelect =
            switchEvents.any { it.containsAction(SwitchAction.Companion.Actions.ACTION_SELECT) }
        val containsNext =
            switchEvents.any { it.containsAction(SwitchAction.Companion.Actions.ACTION_MOVE_TO_NEXT_ITEM) }
        val containsPrevious =
            switchEvents.any { it.containsAction(SwitchAction.Companion.Actions.ACTION_MOVE_TO_PREVIOUS_ITEM) }

        return when (mode.id) {
            ScanMode.Modes.MODE_AUTO ->
                if (containsSelect) null
                else "At least one switch must be configured to the select action."

            ScanMode.Modes.MODE_MANUAL ->
                if (containsSelect && containsNext && containsPrevious) null
                else "At least one switch must be configured to the next, previous, and select actions."

            else -> null
        }
    }
}