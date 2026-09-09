package com.enaboapps.switchify.service.techniques.pointscan.blocks

import android.os.Handler
import android.os.Looper
import android.view.View
import com.enaboapps.switchify.service.gestures.visuals.GestureVisualMotionPolicy
import com.enaboapps.switchify.service.scanning.ScanVisualConstants
import com.enaboapps.switchify.service.techniques.AccessTechniqueUIBase
import com.enaboapps.switchify.service.techniques.pointscan.PointScanSettings
import com.enaboapps.switchify.service.utils.HighlightAnimations
import com.enaboapps.switchify.service.utils.ScreenUtils

/**
 * Owns the single [PointScanGridView] shown while blocks are being scanned.
 *
 * During the line phase the grid is held at a low alpha instead of removed,
 * so the user keeps the spatial context of where the chosen block sits.
 * [holdForLinePhase] arms that behaviour before the block tree stops;
 * [reset] releases it and tears the grid down.
 */
class PointScanBlockGridUI(private val context: android.content.Context) : AccessTechniqueUIBase() {
    private val handler = Handler(Looper.getMainLooper())
    private var gridView: PointScanGridView? = null
    private var heldForLinePhase = false

    fun showGrid() {
        handler.post {
            heldForLinePhase = false
            val gridSize = PointScanSettings.getCursorBlockCount()
            val existing = gridView
            if (existing != null) {
                existing.gridSize = gridSize
                fadeTo(existing, 1f)
                return@post
            }
            val view = PointScanGridView(context).apply { this.gridSize = gridSize }
            addViewDirectly(
                view,
                0,
                0,
                ScreenUtils.getWidth(context),
                ScreenUtils.getHeight(context)
            )
            gridView = view
            if (GestureVisualMotionPolicy.animationsEnabled()) {
                HighlightAnimations.fadeIn(view)
            } else {
                view.alpha = 1f
            }
        }
    }

    /** Keeps the grid on screen, dimmed, when the block tree stops for the line phase. */
    fun holdForLinePhase() {
        handler.post {
            heldForLinePhase = true
            gridView?.let { fadeTo(it, ScanVisualConstants.GRID_DIMMED_ALPHA) }
        }
    }

    fun hideGrid() {
        handler.post {
            val view = gridView
            if (heldForLinePhase && view != null) {
                fadeTo(view, ScanVisualConstants.GRID_DIMMED_ALPHA)
            } else {
                removeGridNow()
            }
        }
    }

    fun reset() {
        handler.post {
            heldForLinePhase = false
            removeGridNow()
        }
    }

    private fun removeGridNow() {
        gridView?.let { view ->
            view.animate().cancel()
            try {
                if (view.parent != null) {
                    super.removeView(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        gridView = null
        super.hide()
    }

    private fun fadeTo(view: View, alpha: Float) {
        view.animate().cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        if (!GestureVisualMotionPolicy.animationsEnabled()) {
            view.alpha = alpha
            return
        }
        view.animate()
            .alpha(alpha)
            .setDuration(ScanVisualConstants.SHOW_DURATION_MS)
            .setInterpolator(ScanVisualConstants.SHOW_INTERPOLATOR)
            .start()
    }
}
