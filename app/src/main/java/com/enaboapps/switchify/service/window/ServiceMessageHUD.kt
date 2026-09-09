package com.enaboapps.switchify.service.window

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enaboapps.switchify.activities.ui.theme.SwitchifyTheme
import com.enaboapps.switchify.service.components.AccessibilityComposeView
import com.enaboapps.switchify.service.components.overlayTween
import com.enaboapps.switchify.service.components.rememberOverlayMotionEnabled
import com.enaboapps.switchify.service.scanning.ScanVisualConstants
import com.enaboapps.switchify.service.window.overlay.OverlayTarget
import com.enaboapps.switchify.service.window.overlay.OverlayTargets
import com.enaboapps.switchify.utils.Resources
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Bottom-of-screen overlay for short service messages, drawn with Compose.
 *
 * Visual style mirrors [MenuHighlightHud]: a rounded surface-container card
 * with a severity-tinted accent edge and icon. Motion uses the shared overlay
 * timings and collapses to instant changes when animations are disabled.
 *
 * Usage:
 * 1. `setup(applicationContext)` from the accessibility service's `onCreate`.
 * 2. `show(ServiceHudMessage(...))`, or the resource-based convenience
 *    overloads that map the legacy type and time pair onto a duration.
 * 3. `dispose()` from `onDestroy`.
 */
class ServiceMessageHUD private constructor() {
    companion object {
        val instance: ServiceMessageHUD by lazy { ServiceMessageHUD() }
        private const val TAG = "ServiceMessageHUD"
        private const val MAX_WIDTH_DP = 600
        private const val MAX_LINES = 5
    }

    private var applicationCtx: Context? = null
    private var composeView: AccessibilityComposeView? = null
    private var attachedTarget: OverlayTarget.Display? = null
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideMessage() }

    // Compose state. The message is kept through the exit animation so the
    // card does not blank while fading out; only visibility toggles.
    private val messageState = mutableStateOf<ServiceHudMessage?>(null)
    private val visibleState = mutableStateOf(false)

    /** Legacy message kinds, kept for the resource-based overloads. */
    enum class MessageType {
        /** Hides itself after the given [Time]. */
        DISAPPEARING,

        /** Stays until cleared or replaced. */
        PERMANENT
    }

    /** Preset durations for disappearing messages. */
    enum class Time(val milliseconds: Long) {
        SHORT(1500),
        MEDIUM(5000),
        LONG(10000)
    }

    fun setup(appCtx: Context) {
        applicationCtx = appCtx.applicationContext
        Log.d(TAG, "ServiceMessageHUD setup")
    }

    /** Shows [message], replacing whatever is currently on screen. */
    fun show(message: ServiceHudMessage) {
        if (applicationCtx == null) {
            Log.e(TAG, "ApplicationContext is null, cannot show message. Call setup() first.")
            return
        }
        handler.post {
            handler.removeCallbacks(hideRunnable)
            ensureComposeViewIsCreated()
            if (!attachIfNeeded(message.target)) return@post
            Log.d(TAG, "Showing message: \"${message.text}\" severity=${message.severity} duration=${message.durationMillis}")
            messageState.value = message
            visibleState.value = true
            message.durationMillis?.let { handler.postDelayed(hideRunnable, it) }
        }
    }

    fun showMessage(
        messageResId: Int,
        messageType: MessageType,
        time: Time = Time.MEDIUM,
        severity: MessageSeverity = MessageSeverity.Info,
        target: OverlayTarget.Display = OverlayTargets.defaultDisplay()
    ) {
        show(
            ServiceHudMessage(
                text = Resources.getString(messageResId),
                severity = severity,
                durationMillis = ServiceHudMessage.durationFor(messageType, time),
                target = target
            )
        )
    }

    fun showMessage(
        messageResId: Int,
        messageArgs: Array<out Any>,
        messageType: MessageType,
        time: Time = Time.MEDIUM,
        severity: MessageSeverity = MessageSeverity.Info,
        target: OverlayTarget.Display = OverlayTargets.defaultDisplay()
    ) {
        show(
            ServiceHudMessage(
                text = Resources.getString(messageResId, *messageArgs),
                severity = severity,
                durationMillis = ServiceHudMessage.durationFor(messageType, time),
                target = target
            )
        )
    }

    fun showMessageText(
        message: String,
        messageType: MessageType,
        time: Time = Time.MEDIUM,
        severity: MessageSeverity = MessageSeverity.Info,
        target: OverlayTarget.Display = OverlayTargets.defaultDisplay()
    ) {
        show(
            ServiceHudMessage(
                text = message,
                severity = severity,
                durationMillis = ServiceHudMessage.durationFor(messageType, time),
                target = target
            )
        )
    }

    fun clearMessage() {
        handler.post { hideMessage() }
    }

    private fun hideMessage() {
        handler.removeCallbacks(hideRunnable)
        visibleState.value = false
    }

    fun dispose() {
        Log.d(TAG, "Disposing ServiceMessageHUD")
        handler.removeCallbacksAndMessages(null)
        composeView?.let { view ->
            try {
                SwitchifyAccessibilityWindow.instance.removeView(
                    attachedTarget ?: OverlayTargets.defaultDisplay(),
                    view
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove HUD view", e)
            }
        }
        composeView = null
        attachedTarget = null
        messageState.value = null
        visibleState.value = false
        applicationCtx = null
    }

    private fun ensureComposeViewIsCreated() {
        val ctx = applicationCtx ?: return
        if (composeView == null) {
            composeView = AccessibilityComposeView(ctx) {
                ServiceMessageUi(
                    message = messageState.value,
                    isVisible = visibleState.value,
                    onDismiss = { hideMessage() }
                )
            }
        }
    }

    /** Attaches the view to [target], moving it if it is on another display. */
    private fun attachIfNeeded(target: OverlayTarget.Display): Boolean {
        val view = composeView ?: return false
        try {
            if (view.parent != null && attachedTarget != target) {
                SwitchifyAccessibilityWindow.instance.removeView(
                    attachedTarget ?: OverlayTargets.defaultDisplay(),
                    view
                )
                attachedTarget = null
            }
            if (view.parent == null) {
                SwitchifyAccessibilityWindow.instance.addViewToBottom(target, view)
                attachedTarget = target
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach HUD view", e)
            return false
        }
    }

    @Composable
    private fun ServiceMessageUi(
        message: ServiceHudMessage?,
        isVisible: Boolean,
        onDismiss: () -> Unit
    ) {
        val motion = rememberOverlayMotionEnabled()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = isVisible && message != null,
                enter = fadeIn(overlayTween(ScanVisualConstants.SHOW_DURATION_MS, motion)) +
                    slideInVertically(overlayTween(ScanVisualConstants.SHOW_DURATION_MS, motion)) { it / 4 },
                exit = fadeOut(overlayTween(ScanVisualConstants.HIDE_DURATION_MS, motion))
            ) {
                val current = message ?: return@AnimatedVisibility
                SwipeableMessageCard(
                    modifier = Modifier
                        .widthIn(max = MAX_WIDTH_DP.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
                    message = current,
                    onDismiss = onDismiss
                ) {
                    MessageCard(message = current, motionEnabled = motion)
                }
            }
        }
    }

    /** Horizontal swipe-to-dismiss wrapper. Kept for carers; switch users get other dismissal paths. */
    @Composable
    private fun SwipeableMessageCard(
        modifier: Modifier = Modifier,
        message: ServiceHudMessage,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit
    ) {
        val offsetX = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(message) {
            offsetX.snapTo(0f)
        }

        Box(
            modifier = modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = size.width * 0.4f
                            if (abs(offsetX.value) > threshold) {
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = if (offsetX.value > 0) size.width.toFloat() else -size.width.toFloat(),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                    onDismiss()
                                }
                            } else {
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }

    @Composable
    private fun MessageCard(message: ServiceHudMessage, motionEnabled: Boolean) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            // Cross-fade the whole row when one message replaces another so
            // the card stays put and only its content changes.
            Crossfade(
                targetState = message,
                animationSpec = overlayTween(ScanVisualConstants.SHOW_DURATION_MS, motionEnabled),
                label = "ServiceMessageContent"
            ) { current ->
                val accent = current.severity.accentColor()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(accent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = current.severity.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = accent
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = current.text,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            lineHeight = 26.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = MAX_LINES,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    @Composable
    private fun MessageSeverity.accentColor(): Color = when (this) {
        MessageSeverity.Info -> MaterialTheme.colorScheme.secondary
        MessageSeverity.Success -> SwitchifyTheme.semanticColors.success
        MessageSeverity.Warning -> SwitchifyTheme.semanticColors.warning
        MessageSeverity.Error -> MaterialTheme.colorScheme.error
    }

    private fun MessageSeverity.icon(): ImageVector = when (this) {
        MessageSeverity.Info -> Icons.Filled.Info
        MessageSeverity.Success -> Icons.Filled.CheckCircle
        MessageSeverity.Warning -> Icons.Filled.Warning
        MessageSeverity.Error -> Icons.Filled.Error
    }
}
