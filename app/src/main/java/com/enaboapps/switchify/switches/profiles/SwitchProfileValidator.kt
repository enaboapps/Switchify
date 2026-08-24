package com.enaboapps.switchify.switches.profiles

import com.enaboapps.switchify.switches.SwitchEvent

internal object SwitchProfileValidator {
    fun validate(
        events: List<SwitchEvent>,
        requiredActionIds: Set<Int>,
        supportedActionIds: Set<Int>
    ): SwitchProfileValidationResult {
        val configured = events.flatMap { event ->
            listOf(event.pressAction.id) + event.holdActions.map { it.id }
        }.toSet()
        val unsupported = configured - supportedActionIds
        val missing = requiredActionIds - configured
        return SwitchProfileValidationResult(
            isValid = unsupported.isEmpty() && missing.isEmpty(),
            missingActionIds = missing,
            unsupportedActionIds = unsupported
        )
    }
}
