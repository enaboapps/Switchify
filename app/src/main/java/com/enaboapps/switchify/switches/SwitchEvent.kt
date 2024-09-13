package com.enaboapps.switchify.switches

data class SwitchEvent(
    val name: String,
    val code: String,
    val pressAction: SwitchAction,
    val holdActions: List<SwitchAction>
) {
    override fun toString(): String {
        val holdActionsString = holdActions.joinToString(separator = ";") { it.id.toString() }
        return "$name, $code, ${pressAction.id}, $holdActionsString"
    }

    override fun equals(other: Any?): Boolean {
        if (other is SwitchEvent) {
            return code == other.code
        }
        return false
    }

    fun containsAction(action: Int): Boolean {
        return pressAction.id == action || holdActions.any { it.id == action }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + pressAction.hashCode()
        result = 31 * result + holdActions.hashCode()
        return result
    }

    companion object {
        fun fromString(string: String): SwitchEvent {
            val parts = string.split(", ")
            val name = parts[0]
            val code = parts[1]
            val pressAction = SwitchAction(parts[2].toInt())
            val holdActionsString = parts[3]
            val holdActions = holdActionsString.split(";").map { SwitchAction(it.toInt()) }
            return SwitchEvent(
                name = name,
                code = code,
                pressAction = pressAction,
                holdActions = holdActions
            )
        }
    }
}
