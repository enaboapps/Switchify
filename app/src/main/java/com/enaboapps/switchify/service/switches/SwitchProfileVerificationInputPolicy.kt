package com.enaboapps.switchify.service.switches

import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.profiles.SwitchProfileActivationState

internal enum class SwitchProfileVerificationInputDecision {
    PASS_THROUGH,
    CONSUME,
    CONFIRM
}

internal object SwitchProfileVerificationInputPolicy {
    fun decide(
        state: SwitchProfileActivationState,
        action: SwitchAction
    ): SwitchProfileVerificationInputDecision {
        if (state !is SwitchProfileActivationState.Verifying) {
            return SwitchProfileVerificationInputDecision.PASS_THROUGH
        }
        return if (action.id == SwitchAction.ACTION_SELECT) {
            SwitchProfileVerificationInputDecision.CONFIRM
        } else {
            SwitchProfileVerificationInputDecision.CONSUME
        }
    }
}
