package com.enaboapps.switchify.service.switches

import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.profiles.SwitchProfile
import com.enaboapps.switchify.switches.profiles.SwitchProfileActivationState
import org.junit.Assert.assertEquals
import org.junit.Test

class SwitchProfileVerificationInputPolicyTest {
    private val profile = SwitchProfile("profile", "Profile", emptyList())
    private val verifying = SwitchProfileActivationState.Verifying(profile, 60_000L)

    @Test
    fun normalInputPassesThroughWithoutVerification() {
        assertEquals(
            SwitchProfileVerificationInputDecision.PASS_THROUGH,
            SwitchProfileVerificationInputPolicy.decide(
                SwitchProfileActivationState.Idle,
                SwitchAction(SwitchAction.ACTION_SELECT)
            )
        )
    }

    @Test
    fun nonSelectStagedActionsAreConsumed() {
        assertEquals(
            SwitchProfileVerificationInputDecision.CONSUME,
            SwitchProfileVerificationInputPolicy.decide(
                verifying,
                SwitchAction(SwitchAction.ACTION_SYS_HOME)
            )
        )
    }

    @Test
    fun stagedSelectConfirmsForEveryDispatchSource() {
        listOf("external_press", "external_hold", "camera").forEach {
            assertEquals(
                it,
                SwitchProfileVerificationInputDecision.CONFIRM,
                SwitchProfileVerificationInputPolicy.decide(
                    verifying,
                    SwitchAction(SwitchAction.ACTION_SELECT)
                )
            )
        }
    }
}
