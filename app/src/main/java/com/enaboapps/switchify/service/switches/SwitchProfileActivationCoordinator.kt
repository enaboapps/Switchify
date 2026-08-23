package com.enaboapps.switchify.service.switches

import android.content.Context
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.camera.CameraPermissionManager
import com.enaboapps.switchify.service.core.ServiceBridge
import com.enaboapps.switchify.service.core.ServiceCore
import com.enaboapps.switchify.service.window.MessageSeverity
import com.enaboapps.switchify.service.window.ServiceMessageHUD
import com.enaboapps.switchify.switches.RequiredActionsPolicy
import com.enaboapps.switchify.switches.SWITCH_EVENT_TYPE_CAMERA
import com.enaboapps.switchify.switches.SupportedActionsPolicy
import com.enaboapps.switchify.switches.SwitchAction
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.switches.profiles.SwitchProfileActivationState
import com.enaboapps.switchify.switches.profiles.SwitchProfileRepository
import com.enaboapps.switchify.switches.profiles.SwitchProfileValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SwitchProfileActivationCoordinator(
    private val context: Context,
    private val switchEventProvider: SwitchEventProvider,
    private val scope: CoroutineScope,
    private val repository: SwitchProfileRepository = SwitchProfileRepository.getInstance(context),
    private val now: () -> Long = System::currentTimeMillis
) {
    private val _state = MutableStateFlow<SwitchProfileActivationState>(SwitchProfileActivationState.Idle)
    val state: StateFlow<SwitchProfileActivationState> = _state.asStateFlow()
    private var timeoutJob: Job? = null
    private var confirmationStarted = false

    fun begin(profileId: String) {
        scope.launch {
            repository.initialize()
            cancel(showMessage = false)
            val profile = repository.profile(profileId)
            if (profile == null) {
                fail(profileId, "not_found")
                return@launch
            }
            if (repository.document.value.activeProfileId == profileId) {
                fail(profileId, "already_active")
                return@launch
            }
            val validation = SwitchProfileValidator.validate(
                profile.switches,
                RequiredActionsPolicy.requiredActionIds(context),
                SupportedActionsPolicy.supportedActionIds(context) + SwitchAction.ACTION_NONE
            )
            if (!validation.isValid) {
                fail(
                    profileId,
                    "invalid",
                    validation.missingActionIds,
                    validation.unsupportedActionIds
                )
                return@launch
            }
            if (profile.switches.any { it.type == SWITCH_EVENT_TYPE_CAMERA } &&
                !CameraPermissionManager.getInstance(context).hasPermission()
            ) {
                fail(profileId, "camera_permission")
                return@launch
            }
            val expiresAt = now() + VERIFICATION_TIMEOUT_MS
            confirmationStarted = false
            switchEventProvider.stage(profile.switches)
            ServiceCore.getCameraManager()?.evaluateAndUpdateCameraState()
            _state.value = SwitchProfileActivationState.Verifying(profile, expiresAt)
            ServiceBridge.emitEvent(
                ServiceBridge.ServiceEvent.SwitchProfileVerificationStarted(
                    profile.id,
                    profile.name,
                    expiresAt
                )
            )
            timeoutJob = scope.launch {
                while (true) {
                    val remaining = (expiresAt - now()).coerceAtLeast(0L)
                    if (remaining == 0L) break
                    ServiceMessageHUD.instance.showMessageText(
                        context.getString(
                            R.string.switch_profile_verification_prompt,
                            profile.name,
                            (remaining + 999L) / 1000L
                        ),
                        ServiceMessageHUD.MessageType.PERMANENT,
                        severity = MessageSeverity.Info
                    )
                    delay(1000L)
                }
                cancel("timeout")
            }
        }
    }

    fun intercept(action: SwitchAction): Boolean {
        return when (SwitchProfileVerificationInputPolicy.decide(_state.value, action)) {
            SwitchProfileVerificationInputDecision.PASS_THROUGH -> false
            SwitchProfileVerificationInputDecision.CONSUME -> true
            SwitchProfileVerificationInputDecision.CONFIRM -> {
                val verifying = _state.value as SwitchProfileActivationState.Verifying
                if (!confirmationStarted) {
                    confirmationStarted = true
                    scope.launch { confirm(verifying) }
                }
                true
            }
        }
    }

    fun cancel(reason: String = "cancelled", showMessage: Boolean = true) {
        val wasVerifying = _state.value is SwitchProfileActivationState.Verifying
        if (!wasVerifying) return
        timeoutJob?.cancel()
        timeoutJob = null
        confirmationStarted = false
        switchEventProvider.clearStage()
        ServiceCore.getCameraManager()?.evaluateAndUpdateCameraState()
        _state.value = SwitchProfileActivationState.Idle
        confirmationStarted = false
        ServiceMessageHUD.instance.clearMessage()
        if (wasVerifying) {
            ServiceBridge.emitEvent(ServiceBridge.ServiceEvent.SwitchProfileActivationCancelled)
            if (showMessage) {
                ServiceMessageHUD.instance.showMessage(
                    if (reason == "timeout") R.string.switch_profile_verification_timed_out
                    else R.string.switch_profile_verification_cancelled,
                    ServiceMessageHUD.MessageType.DISAPPEARING,
                    severity = MessageSeverity.Warning
                )
            }
        }
    }

    private suspend fun confirm(verifying: SwitchProfileActivationState.Verifying) {
        timeoutJob?.cancel()
        timeoutJob = null
        val previousProfileId = repository.document.value.activeProfileId
        if (!repository.commitActiveProfile(verifying.profile.id)) {
            switchEventProvider.clearStage()
            ServiceCore.getCameraManager()?.evaluateAndUpdateCameraState()
            fail(verifying.profile.id, "storage")
            return
        }
        if (!switchEventProvider.promoteStage()) {
            repository.commitActiveProfile(previousProfileId)
            switchEventProvider.clearStage()
            ServiceCore.getCameraManager()?.evaluateAndUpdateCameraState()
            fail(verifying.profile.id, "runtime_reload")
            return
        }
        SwitchEventStore.getInstance().refreshActiveProfile(context)
        ServiceCore.getCameraManager()?.evaluateAndUpdateCameraState()
        _state.value = SwitchProfileActivationState.Activated(verifying.profile)
        ServiceBridge.emitEvent(
            ServiceBridge.ServiceEvent.SwitchProfileActivated(
                verifying.profile.id,
                verifying.profile.name
            )
        )
        ServiceBridge.emitEvent(ServiceBridge.ServiceEvent.SwitchProfilesUpdated)
        ServiceMessageHUD.instance.showMessage(
            R.string.switch_profile_activated,
            arrayOf(verifying.profile.name),
            ServiceMessageHUD.MessageType.DISAPPEARING,
            severity = MessageSeverity.Success
        )
        _state.value = SwitchProfileActivationState.Idle
    }

    private fun fail(
        profileId: String,
        reason: String,
        missingActionIds: Set<Int> = emptySet(),
        unsupportedActionIds: Set<Int> = emptySet()
    ) {
        switchEventProvider.clearStage()
        confirmationStarted = false
        _state.value = SwitchProfileActivationState.Failed(profileId, reason)
        ServiceBridge.emitEvent(
            ServiceBridge.ServiceEvent.SwitchProfileActivationFailed(
                profileId,
                reason,
                missingActionIds,
                unsupportedActionIds
            )
        )
        val message = when (reason) {
            "camera_permission" -> context.getString(R.string.switch_profile_camera_permission_required)
            "invalid" -> when {
                missingActionIds.isNotEmpty() -> context.getString(
                    R.string.switch_profile_missing_actions,
                    missingActionIds.joinToString { SwitchAction(it).getActionName() }
                )
                else -> context.getString(
                    R.string.switch_profile_unsupported_actions,
                    unsupportedActionIds.joinToString { SwitchAction(it).getActionName() }
                )
            }
            else -> context.getString(R.string.switch_profile_activation_failed)
        }
        ServiceMessageHUD.instance.showMessageText(
            message,
            ServiceMessageHUD.MessageType.DISAPPEARING,
            severity = MessageSeverity.Error
        )
        _state.value = SwitchProfileActivationState.Idle
    }

    private companion object {
        const val VERIFICATION_TIMEOUT_MS = 60_000L
    }
}
