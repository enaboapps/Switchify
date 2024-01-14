package com.enaboapps.switchify.switches

import android.content.Context
import android.util.Log
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
        if (switchEvents.remove(switchEvent)) {
            switchEvents.add(switchEvent)
            saveToFile()
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
}