package com.enaboapps.switchify.switches

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.preferences.PreferenceManager
import com.enaboapps.switchify.service.scanning.ScanMode
import java.io.File

class SwitchEventStore(private val context: Context) {

    private val switchEvents = mutableSetOf<SwitchEvent>()
    private val fileName = "switch_events.txt"

    init {
        readFile()
    }

    fun add(switchEvent: SwitchEvent) {
        if (switchEvents.add(switchEvent)) {
            saveToFile()
        }
    }

    fun update(switchEvent: SwitchEvent) {
        val file = File(context.applicationContext.filesDir, fileName)
        if (file.exists()) {
            try {
                val lines = file.readLines()
                val newLines = lines.map { line ->
                    val parts = line.split(", ")
                    if (parts[1] == switchEvent.code) {
                        switchEvent.toString()
                    } else {
                        line
                    }
                }
                file.writeText(newLines.joinToString("\n"))
            } catch (e: Exception) {
                Log.e("SwitchEventStore", "Error reading from file", e)
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

    fun find(code: String): SwitchEvent? {
        for (switchEvent in switchEvents) {
            Log.d("SwitchEventStore", "Checking switch event ${switchEvent.code} for code $code")
            if (switchEvent.code == code) {
                Log.d("SwitchEventStore", "Found switch event for code $code")
                return switchEvent
            }
        }
        Log.d("SwitchEventStore", "No switch event found for code $code")
        return null
    }

    fun getCount(): Int = switchEvents.size

    fun getSwitchEvents(): Set<SwitchEvent> = switchEvents.toSet()

    private fun readFile() {
        val file = File(context.applicationContext.filesDir, fileName)
        if (file.exists()) {
            try {
                file.readLines().forEach { line ->
                    try {
                        SwitchEvent.fromString(line).let { switchEvents.add(it) }
                    } catch (e: Exception) {
                        Log.e("SwitchEventStore", "Error parsing line: $line", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("SwitchEventStore", "Error reading from file", e)
            }
        } else {
            Log.d("SwitchEventStore", "File does not exist")
        }
    }

    private fun saveToFile() {
        val file = File(context.applicationContext.filesDir, fileName)
        try {
            file.writeText(switchEvents.joinToString("\n") { it.toString() })
        } catch (e: Exception) {
            Log.e("SwitchEventStore", "Error writing to file", e)
        }
    }

    // This function returns a string to tell the user if the configuration is valid
    // If mode is auto, at least one switch must be configured to the select action
    // If mode is manual, at least one switch must be configured to the next and previous actions
    // and at least one switch must be configured to the select action
    fun isConfigInvalid(): String? {
        val preferenceManager = PreferenceManager(context)
        val mode =
            ScanMode(preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_SCAN_MODE))
        val containsSelect =
            switchEvents.any { it.containsAction(SwitchAction.Actions.ACTION_SELECT) }
        val containsNext =
            switchEvents.any { it.containsAction(SwitchAction.Actions.ACTION_MOVE_TO_NEXT_ITEM) }
        val containsPrevious =
            switchEvents.any { it.containsAction(SwitchAction.Actions.ACTION_MOVE_TO_PREVIOUS_ITEM) }
        return when (mode.id) {
            ScanMode.Modes.MODE_AUTO -> if (containsSelect) {
                null
            } else {
                "At least one switch must be configured to the select action."
            }

            ScanMode.Modes.MODE_MANUAL -> if (containsSelect && containsNext && containsPrevious) {
                null
            } else {
                "At least one switch must be configured to the next, previous, and select actions."
            }

            else -> null
        }
    }
}