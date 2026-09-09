package com.enaboapps.switchify.service.window

/** How the HUD should draw the current message. */
internal enum class HudPresentation {
    /** Compact transient pill. */
    TOAST,

    /** Full status card. */
    BANNER,

    /** Collapsed status: icon plus one line. */
    CHIP
}

/**
 * What the HUD should show right now and when it next needs a [ServiceHudMessageController.tick].
 */
internal data class HudFrame(
    val message: ServiceHudMessage?,
    val presentation: HudPresentation?,
    val nextTickAt: Long?
) {
    companion object {
        val HIDDEN = HudFrame(null, null, null)
    }
}

/**
 * Pure state machine behind [ServiceMessageHUD]. All times are caller-supplied
 * milliseconds on one monotonic clock, so the rules can be unit tested.
 *
 * Rules:
 * - A toast shows immediately if nothing is showing, or if the current toast
 *   has been up for at least [minDisplayMillis] and nothing is queued.
 *   Otherwise it queues. A toast whose key matches the one on screen replaces
 *   it in place; a key match in the queue replaces that entry.
 * - A status message replaces any previous status. It shows as a banner when
 *   no toast is up, collapses to a chip after [collapseAfterMillis], and
 *   returns after toasts finish.
 * - [dismiss] ends whatever is on screen: a toast advances to the next, a
 *   status is dropped.
 */
internal class ServiceHudMessageController(
    private val minDisplayMillis: Long = MIN_DISPLAY_MS,
    private val collapseAfterMillis: Long = COLLAPSE_AFTER_MS
) {
    companion object {
        const val MIN_DISPLAY_MS = 1200L
        const val COLLAPSE_AFTER_MS = 8000L
    }

    private var status: ServiceHudMessage? = null
    private var statusShownAt: Long? = null
    private var statusCollapsed = false
    private val queue = ArrayDeque<ServiceHudMessage>()
    private var toast: ServiceHudMessage? = null
    private var toastShownAt = 0L
    private var toastHideAt = 0L

    fun show(message: ServiceHudMessage, now: Long): HudFrame {
        if (message.isStatus) {
            status = message
            statusCollapsed = false
            statusShownAt = if (toast == null) now else null
            return frame(now)
        }
        val current = toast
        when {
            current == null -> display(message, now)
            message.key != null && message.key == current.key -> display(message, now)
            queue.isEmpty() && now - toastShownAt >= minDisplayMillis -> display(message, now)
            else -> enqueue(message)
        }
        return frame(now)
    }

    fun tick(now: Long): HudFrame {
        val current = toast
        if (current != null) {
            if (queue.isNotEmpty() && now - toastShownAt >= minDisplayMillis) {
                display(queue.removeFirst(), now)
            } else if (now >= toastHideAt) {
                endToast(now)
            }
        } else if (status != null && !statusCollapsed) {
            val shownAt = statusShownAt
            if (shownAt != null && now - shownAt >= collapseAfterMillis) statusCollapsed = true
        }
        return frame(now)
    }

    fun dismiss(now: Long): HudFrame {
        if (toast != null) {
            endToast(now)
        } else {
            status = null
            statusShownAt = null
            statusCollapsed = false
        }
        return frame(now)
    }

    fun clear(now: Long): HudFrame {
        status = null
        statusShownAt = null
        statusCollapsed = false
        queue.clear()
        toast = null
        return frame(now)
    }

    private fun endToast(now: Long) {
        toast = null
        if (queue.isNotEmpty()) {
            display(queue.removeFirst(), now)
        } else if (status != null && !statusCollapsed) {
            statusShownAt = now
        }
    }

    private fun display(message: ServiceHudMessage, now: Long) {
        toast = message
        toastShownAt = now
        toastHideAt = now + (message.durationMillis ?: 0L)
    }

    private fun enqueue(message: ServiceHudMessage) {
        if (message.key != null) {
            val index = queue.indexOfFirst { it.key == message.key }
            if (index >= 0) {
                queue[index] = message
                return
            }
        }
        queue.addLast(message)
    }

    private fun frame(now: Long): HudFrame {
        toast?.let { current ->
            val next = if (queue.isNotEmpty()) {
                minOf(toastHideAt, toastShownAt + minDisplayMillis)
            } else {
                toastHideAt
            }
            return HudFrame(current, HudPresentation.TOAST, next)
        }
        val banner = status ?: return HudFrame.HIDDEN
        if (statusCollapsed) return HudFrame(banner, HudPresentation.CHIP, null)
        val shownAt = statusShownAt ?: now.also { statusShownAt = it }
        return HudFrame(banner, HudPresentation.BANNER, shownAt + collapseAfterMillis)
    }
}
