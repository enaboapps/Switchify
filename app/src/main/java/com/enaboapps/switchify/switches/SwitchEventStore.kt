package com.enaboapps.switchify.switches

import android.content.Context
import java.io.File

class SwitchEventStore(private val context: Context) {

    private val switchEvents = mutableListOf<SwitchEvent>()

    private val fileName = "switch_events.txt"

    fun add(switchEvent: SwitchEvent) {
        if (!switchEvents.contains(switchEvent)) {
            switchEvents.add(switchEvent)
            saveToFile()
        }
    }

    fun remove(switchEvent: SwitchEvent) {
        if (switchEvents.contains(switchEvent)) {
            switchEvents.remove(switchEvent)
            saveToFile()
        }
    }

    fun contains(switchEvent: SwitchEvent): Boolean {
        return switchEvents.contains(switchEvent)
    }

    fun getCount(): Int {
        return switchEvents.size
    }

    fun getSwitchEvents(): List<SwitchEvent> {
        readFile()
        return switchEvents
    }

    // Function to read the file
    private fun readFile() {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val lines = file.readLines()
            for (line in lines) {
                val switchEvent = SwitchEvent.fromString(line)
                switchEvents.add(switchEvent)
            }
        }
    }

    // Function to save the switch events to a file
    private fun saveToFile() {
        val file = File(context.filesDir, fileName)
        // Check if the file exists
        if (file.exists()) {
            // If it does, delete it
            file.delete()
        }
        // Create a new file
        file.createNewFile()
        for (switchEvent in switchEvents) {
            file.appendText(switchEvent.toString() + "\n")
        }
    }

}