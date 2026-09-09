package com.enaboapps.switchify.service.techniques.nodes.scanners

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.RelativeLayout
import androidx.core.graphics.toColorInt
import com.enaboapps.switchify.service.scanning.ScanColorManager
import com.enaboapps.switchify.service.scanning.ScanHighlightDrawable
import com.enaboapps.switchify.service.scanning.ScanHighlightStyle
import com.enaboapps.switchify.service.scanning.ScanIntervalEvent
import com.enaboapps.switchify.service.scanning.ScanIntervalStore
import com.enaboapps.switchify.service.scanning.ScanVisualConstants
import com.enaboapps.switchify.service.utils.HighlightAnimations
import com.enaboapps.switchify.service.window.MenuHighlightHud
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow
import com.enaboapps.switchify.service.window.overlay.OverlayTarget
import com.enaboapps.switchify.service.window.overlay.OverlayTargets
import java.util.concurrent.atomic.AtomicLong

internal fun interface NodeScannerUiDispatcher {
    fun post(block: () -> Unit)
}

private object MainNodeScannerUiDispatcher : NodeScannerUiDispatcher {
    private val handler = Handler(Looper.getMainLooper())

    override fun post(block: () -> Unit) {
        handler.post(block)
    }
}

internal interface NodeScannerOverlayWindow {
    fun getContext(): Context?
    fun getDisplaySize(target: OverlayTarget): Pair<Int, Int>?
    fun addView(
        target: OverlayTarget,
        view: RelativeLayout,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    )
    fun removeView(target: OverlayTarget, view: RelativeLayout)
}

private object SwitchifyNodeScannerOverlayWindow : NodeScannerOverlayWindow {
    private val window: SwitchifyAccessibilityWindow
        get() = SwitchifyAccessibilityWindow.instance

    override fun getContext(): Context? = window.getContext()

    override fun getDisplaySize(target: OverlayTarget): Pair<Int, Int>? {
        val metrics = window.getDisplayMetrics(target) ?: return null
        return metrics.width to metrics.height
    }

    override fun addView(
        target: OverlayTarget,
        view: RelativeLayout,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        window.addView(target, view, x, y, width, height)
    }

    override fun removeView(target: OverlayTarget, view: RelativeLayout) {
        window.removeView(target, view)
    }
}

class NodeScannerUI internal constructor(
    private val window: NodeScannerOverlayWindow = SwitchifyNodeScannerOverlayWindow,
    private val dispatcher: NodeScannerUiDispatcher = MainNodeScannerUiDispatcher
) {
    companion object {
        val instance: NodeScannerUI by lazy { NodeScannerUI() }

        private val itemRoles = setOf(NodeScannerHighlightRole.ITEM)
        private val rowRoles = setOf(
            NodeScannerHighlightRole.ROW,
            NodeScannerHighlightRole.ESCAPE
        )
    }

    private data class ActiveHighlight(
        val state: NodeScannerHighlightState,
        val view: RelativeLayout,
        val spec: NodeScannerHighlightSpec
    )

    private val commandLock = Any()
    private val rendererEpoch = AtomicLong(0L)

    private val visualBatch = ThreadLocal<NodeScannerVisualBatch?>()
    private var movement: ValueAnimator? = null
    private var movementGeneration = 0L
    private val intervalSequence = AtomicLong()
    private val intervals = ScanIntervalStore()
    private var spotlightUnavailable: OverlayTarget? = null

    internal fun withScanVisuals(owner: String?, block: () -> Unit) {
        if (owner == null || visualBatch.get()?.owner == owner) {
            block()
            return
        }
        val previous = visualBatch.get()
        val batch = NodeScannerVisualBatch(owner, rendererEpoch.get(), intervalSequence.get())
        visualBatch.set(batch)
        try {
            block()
        } finally {
            visualBatch.set(previous)
            synchronized(commandLock) {
                dispatcher.post {
                    if (batch.epoch == rendererEpoch.get()) {
                        batch.spec?.let(::render) ?: hideHighlight(batch.hideRoles)
                    }
                }
            }
        }
    }

    fun updateInterval(event: ScanIntervalEvent) {
        val sequence = intervalSequence.incrementAndGet()
        submitCurrentEpoch {
            intervals.record(event, sequence)
            updateCountdown()
        }
    }

    fun refreshPreferences() {
        submitCurrentEpoch {
            val active = activeHighlight ?: return@submitCurrentEpoch
            render(active.spec)
        }
    }

    private var style: ScanHighlightStyle? = null
    private var baseLayout: RelativeLayout? = null
    private var activeHighlight: ActiveHighlight? = null
    private var overlayTarget: OverlayTarget = OverlayTargets.defaultDisplay()

    fun showItemBounds(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        target: OverlayTarget = OverlayTargets.defaultDisplay(),
        screenBounds: ScanHighlightBounds? = null
    ) {
        showHighlight(
            NodeScannerHighlightSpec(
                NodeScannerHighlightRole.ITEM,
                x,
                y,
                width,
                height,
                target,
                visualBatch.get()?.owner,
                screenBounds
            )
        )
    }

    fun showRowBounds(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        target: OverlayTarget = OverlayTargets.defaultDisplay(),
        screenBounds: ScanHighlightBounds? = null
    ) {
        showHighlight(
            NodeScannerHighlightSpec(
                NodeScannerHighlightRole.ROW,
                x,
                y,
                width,
                height,
                target,
                visualBatch.get()?.owner,
                screenBounds
            )
        )
    }

    fun showEscapeBounds(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        target: OverlayTarget = OverlayTargets.defaultDisplay(),
        screenBounds: ScanHighlightBounds? = null
    ) {
        showHighlight(
            NodeScannerHighlightSpec(
                NodeScannerHighlightRole.ESCAPE,
                x,
                y,
                width,
                height,
                target,
                visualBatch.get()?.owner,
                screenBounds
            )
        )
    }

    fun hideItemBounds() {
        if (batchHide(itemRoles)) return
        submitCurrentEpoch {
            hideHighlight(itemRoles)
        }
    }

    fun hideRowBounds() {
        if (batchHide(rowRoles)) return
        submitCurrentEpoch {
            hideHighlight(rowRoles)
        }
    }

    fun hideAll() {
        synchronized(commandLock) {
            val epoch = rendererEpoch.incrementAndGet()
            visualBatch.get()?.reset(epoch)
            dispatcher.post {
                if (NodeScannerHighlightTransitions.isCurrentEpoch(epoch, rendererEpoch.get())) {
                    hideAllNow()
                }
            }
        }
    }

    private fun batchHide(roles: Set<NodeScannerHighlightRole>): Boolean {
        val batch = visualBatch.get() ?: return false
        batch.hide(roles)
        return true
    }

    private fun showHighlight(spec: NodeScannerHighlightSpec) {
        visualBatch.get()?.let {
            it.show(spec)
            return
        }
        submitCurrentEpoch {
            render(spec)
        }
    }

    private fun submitCurrentEpoch(block: () -> Unit) {
        synchronized(commandLock) {
            val epoch = rendererEpoch.get()
            dispatcher.post {
                if (NodeScannerHighlightTransitions.isCurrentEpoch(epoch, rendererEpoch.get())) {
                    block()
                }
            }
        }
    }

    private fun render(spec: NodeScannerHighlightSpec) {
        val context = window.getContext() ?: return
        val highlightStyle = style ?: ScanHighlightStyle(context).also { style = it }
        val spotlight = spec.usesSpotlight(highlightStyle.isSpotlight(), spotlightUnavailable)
        val effectiveTarget = if (spotlight) {
            when (val target = spec.target) {
                is OverlayTarget.Display -> target.copy(forceSurface = target.displayId != OverlayTargets.DEFAULT_DISPLAY_ID)
                is OverlayTarget.Window -> OverlayTarget.Display(target.displayId,
                    forceSurface = target.displayId != OverlayTargets.DEFAULT_DISPLAY_ID)
            }
        } else spec.target
        val current = activeHighlight
        val replace = current != null && (!NodeScannerHighlightTransitions.sameCoordinateSpace(current.state.target, effectiveTarget) ||
            current.spec.owner != spec.owner)
        if (replace) removeBaseLayoutNow()
        val active = activeHighlight
        if (active == null) {
            val base = prepareBaseLayout(effectiveTarget, context) ?: return
            val view = if (spec.owner != null) ScanHighlightView(context, OverlayTargets.displayFallback(effectiveTarget).displayId) {
                if (activeHighlight?.view === it) hideAll()
            } else RelativeLayout(context)
            activeHighlight = ActiveHighlight(NodeScannerHighlightState(spec.role, effectiveTarget), view, spec)
            base.addView(view)
            applySpec(view, spec, spotlight, animate = false)
            if (spec.owner == null) HighlightAnimations.fadeIn(view)
            if (spotlight) {
                MenuHighlightHud.instance.bringToFront()
                dispatcher.post {
                    if (baseLayout === base && !base.isAttachedToWindow && activeHighlight?.spec == spec) {
                        spotlightUnavailable = spec.target
                        removeBaseLayoutNow()
                        render(spec)
                    }
                }
            }
        } else {
            activeHighlight = ActiveHighlight(NodeScannerHighlightState(spec.role, effectiveTarget), active.view, spec)
            applySpec(active.view, spec, spotlight,
                animate = spec.animatesFrom(active.spec, highlightStyle.isMovementEnabled(), ValueAnimator.areAnimatorsEnabled()))
        }
        updateCountdown()
    }

    private fun applySpec(view: RelativeLayout, spec: NodeScannerHighlightSpec, spotlight: Boolean, animate: Boolean) {
        val context = window.getContext() ?: return
        val highlightStyle = style ?: ScanHighlightStyle(context).also { style = it }
        val colors = ScanColorManager.getScanColorSetFromPreferences(context)
        val color = if (spec.role == NodeScannerHighlightRole.ITEM) colors.secondaryColor else colors.primaryColor
        val drawable = ScanHighlightDrawable(context,
            if (spec.owner == null) highlightStyle.isLegacyFill() else highlightStyle.isFill(), color,
            isDashed = spec.role == NodeScannerHighlightRole.ESCAPE)
        if (view !is ScanHighlightView) {
            normalize(view)
            view.layoutParams = RelativeLayout.LayoutParams(spec.width, spec.height).apply {
                leftMargin = spec.x
                topMargin = spec.y
            }
            view.background = drawable
            return
        }
        view.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT)
        view.highlightDrawable = drawable
        view.spotlight = spotlight
        val colorInt = color.toColorInt()
        view.progressColor = if (Color.red(colorInt) * 0.299 + Color.green(colorInt) * 0.587 +
            Color.blue(colorInt) * 0.114 > 127.5) Color.BLACK else Color.WHITE
        val bounds = if (spotlight) spec.screenBounds!! else
            ScanHighlightBounds(spec.x.toFloat(), spec.y.toFloat(), spec.width.toFloat(), spec.height.toFloat())
        val from = view.highlightBounds
        cancelMovement()
        if (!animate || !from.isUsable || from == bounds) {
            view.highlightBounds = bounds
            return
        }
        val epoch = rendererEpoch.get()
        val generation = movementGeneration
        movement = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ScanVisualConstants.SHOW_DURATION_MS
            interpolator = ScanVisualConstants.SHOW_INTERPOLATOR
            addUpdateListener {
                if (rendererEpoch.get() == epoch && movementGeneration == generation && activeHighlight?.view === view) {
                    view.highlightBounds = from.interpolate(bounds, it.animatedValue as Float)
                }
            }
            start()
        }
    }

    private fun updateCountdown() {
        val active = activeHighlight ?: return
        val view = active.view as? ScanHighlightView ?: return
        view.interval = if (active.spec.role != NodeScannerHighlightRole.ESCAPE &&
            style?.isCountdownEnabled() == true) {
            intervals.intervalFor(active.spec.owner, active.spec.intervalAfterSequence)
        } else null
    }

    private fun cancelMovement() {
        movementGeneration++
        movement?.cancel()
        movement = null
    }

    private fun prepareBaseLayout(
        target: OverlayTarget,
        context: Context
    ): RelativeLayout? {
        if (baseLayout != null && overlayTarget != target) {
            removeBaseLayoutNow()
        }
        overlayTarget = target
        if (baseLayout == null) {
            val base = RelativeLayout(context)
            val fallbackMetrics = context.resources.displayMetrics
            val displaySize = window.getDisplaySize(target)
            baseLayout = base
            window.addView(
                target,
                base,
                0,
                0,
                displaySize?.first ?: fallbackMetrics.widthPixels,
                displaySize?.second ?: fallbackMetrics.heightPixels
            )
        }
        return baseLayout
    }

    private fun hideHighlight(roles: Set<NodeScannerHighlightRole>) {
        if (
            NodeScannerHighlightTransitions.hide(
                activeHighlight?.state,
                roles
            ) == NodeScannerHighlightTransition.REMOVE
        ) {
            removeActiveHighlightNow()
        }
    }

    private fun removeActiveHighlightNow() {
        cancelMovement()
        val active = activeHighlight ?: return
        (active.view as? ScanHighlightView)?.interval = null
        activeHighlight = null
        active.view.animate().cancel()
        baseLayout?.removeView(active.view)
    }

    private fun removeBaseLayoutNow() {
        removeActiveHighlightNow()
        val base = baseLayout ?: return
        val target = overlayTarget
        baseLayout = null
        window.removeView(target, base)
    }

    private fun hideAllNow() {
        intervals.clear()
        spotlightUnavailable = null
        removeBaseLayoutNow()
    }

    private fun normalize(view: RelativeLayout) {
        view.animate().cancel()
        view.alpha = 1f
        view.scaleX = 1f
        view.scaleY = 1f
    }
}
