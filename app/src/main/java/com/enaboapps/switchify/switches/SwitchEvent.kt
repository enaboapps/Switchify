package com.enaboapps.switchify.switches

data class SwitchEvent(
    val name: String,
    val code: String,
    val pressAction: SwitchAction,
    val longPressAction: SwitchAction
) {
    override fun toString(): String {
        return "$name, $code, ${pressAction.id}, ${longPressAction.id}"
    }

    override fun equals(other: Any?): Boolean {
        if (other is SwitchEvent) {
            return code == other.code
        }
        return false
    }

    fun containsAction(action: Int): Boolean {
        return pressAction.id == action || longPressAction.id == action
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + pressAction.hashCode()
        result = 31 * result + longPressAction.hashCode()
        return result
    }

    companion object {
        fun fromString(string: String): SwitchEvent {
            val parts = string.split(", ")
            return SwitchEvent(
                name = parts[0],
                code = parts[1],
                pressAction = SwitchAction(parts[2].toInt()),
                longPressAction = SwitchAction(parts[3].toInt())
            )
        }
    }
}
