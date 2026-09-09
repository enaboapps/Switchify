package com.enaboapps.switchify.service.window

import com.enaboapps.switchify.service.window.overlay.OverlayTarget
import com.enaboapps.switchify.service.window.overlay.OverlayTargets

/** Visual severity of a service HUD message. Colours and icons are resolved in the HUD's composable. */
enum class MessageSeverity {
    Info,
    Success,
    Warning,
    Error
}

/**
 * One message for [ServiceMessageHUD].
 *
 * @property durationMillis How long to show the message before it hides
 * itself, or null to keep it until it is cleared or replaced.
 */
data class ServiceHudMessage(
    val text: String,
    val severity: MessageSeverity = MessageSeverity.Info,
    val durationMillis: Long? = ServiceMessageHUD.Time.MEDIUM.milliseconds,
    val target: OverlayTarget.Display = OverlayTargets.defaultDisplay()
) {
    companion object {
        /** Maps the legacy type and time pair onto a duration; permanent messages have none. */
        fun durationFor(type: ServiceMessageHUD.MessageType, time: ServiceMessageHUD.Time): Long? =
            if (type == ServiceMessageHUD.MessageType.DISAPPEARING) time.milliseconds else null
    }
}
