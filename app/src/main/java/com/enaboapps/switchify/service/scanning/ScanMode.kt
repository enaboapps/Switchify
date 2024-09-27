package com.enaboapps.switchify.service.scanning

/**
 * This class represents a scanning mode.
 * @property id The id of the scanning mode.
 */
class ScanMode(val id: String) {

    /**
     * This object represents the different modes available.
     */
    object Modes {
        const val MODE_AUTO = "auto"
        const val MODE_MANUAL = "manual"
    }

    companion object {
        /**
         * This function returns the scanning modes.
         * @return The scanning modes.
         */
        val modes = listOf(
            ScanMode(Modes.MODE_AUTO),
            ScanMode(Modes.MODE_MANUAL)
        )

        /**
         * This function returns the scanning mode with the given id.
         * @param id The id of the scanning mode.
         * @return The scanning mode with the given id.
         */
        fun fromId(id: String): ScanMode {
            return modes.firstOrNull { it.id == id } ?: modes[0]
        }
    }

    /**
     * This function returns the name of the scanning mode.
     * @return The name of the scanning mode.
     */
    fun getModeName(): String {
        return when (id) {
            Modes.MODE_AUTO -> "Auto"
            Modes.MODE_MANUAL -> "Manual"
            else -> "Unknown"
        }
    }

    /**
     * This function returns the description of the scanning mode.
     * @return The description of the scanning mode.
     */
    fun getModeDescription(): String {
        return when (id) {
            Modes.MODE_AUTO -> "Automatically scan and use a single switch to select."
            Modes.MODE_MANUAL -> "Use different switches to move between items and another switch to select."
            else -> "Unknown"
        }
    }

    /**
     * This function checks if this scanning mode is equal to another object.
     * @param other The object to compare with this scanning mode.
     * @return True if the other object is a scanning mode with the same id, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (other is ScanMode) {
            return other.id == id
        }
        return false
    }

    /**
     * This function returns the hash code of this scanning mode.
     * @return The hash code of this scanning mode.
     */
    override fun hashCode(): Int {
        return id.hashCode()
    }
}