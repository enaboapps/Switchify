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
 * A message with a [durationMillis] is a toast: it queues behind whatever is
 * showing and hides itself. A message without one is a status banner: it
 * stays until cleared or replaced, sits underneath any toasts, and collapses
 * to a chip after a while so it stops covering content.
 *
 * @property key Messages sharing a key replace each other in place instead
 * of queueing, so a stream of progress updates never piles up.
 */
data class ServiceHudMessage(
    val text: String,
    val severity: MessageSeverity = MessageSeverity.Info,
    val durationMillis: Long? = ServiceMessageHUD.Time.MEDIUM.milliseconds,
    val target: OverlayTarget.Display = OverlayTargets.defaultDisplay(),
    val key: String? = null
) {
    val isStatus: Boolean get() = durationMillis == null

    companion object {
        /** Maps the legacy type and time pair onto a duration; permanent messages have none. */
        fun durationFor(type: ServiceMessageHUD.MessageType, time: ServiceMessageHUD.Time): Long? =
            if (type == ServiceMessageHUD.MessageType.DISAPPEARING) time.milliseconds else null
    }
}
