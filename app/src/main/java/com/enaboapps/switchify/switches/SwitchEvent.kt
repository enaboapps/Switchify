package com.enaboapps.switchify.switches

data class SwitchEvent(
    val name: String,
    val code: String,
    val pressAction: SwitchAction,
    val longPressAction: SwitchAction? = null,
) {
    override fun toString(): String {
        return if (longPressAction == null) {
            "$name, $code, ${pressAction.id}"
        } else {
            "$name, $code, ${pressAction.id}, ${longPressAction.id}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is SwitchEvent) {
            return code == other.code
        }
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + pressAction.hashCode()
        result = 31 * result + (longPressAction?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun fromString(string: String): SwitchEvent {
            val parts = string.split(", ")
            return if (parts.size == 3) {
                SwitchEvent(parts[0], parts[1], SwitchAction(parts[2].toInt()))
            } else {
                SwitchEvent(parts[0], parts[1], SwitchAction(parts[2].toInt()), SwitchAction(parts[3].toInt()))
            }
        }
    }
}
