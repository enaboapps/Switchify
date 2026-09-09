package com.enaboapps.switchify.service.techniques.nodes.scanners

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanInterval
import com.enaboapps.switchify.service.scanning.ScanVisualConstants
import kotlin.math.min
import kotlin.math.roundToInt

internal class ScanHighlightView(
    context: Context,
    private val targetDisplayId: Int,
    private val onDisplayInvalidated: (ScanHighlightView) -> Unit
) : RelativeLayout(context) {
    private val displayManager = context.getSystemService(DisplayManager::class.java)
    private fun displayGeometry(): Triple<Int, Int, Int>? = displayManager?.getDisplay(targetDisplayId)?.let {
        Triple(it.rotation, it.mode.physicalWidth, it.mode.physicalHeight)
    }
    private var geometry = displayGeometry()
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) {
            if (displayId == targetDisplayId) onDisplayInvalidated(this@ScanHighlightView)
        }
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == targetDisplayId && geometry != displayGeometry()) {
                onDisplayInvalidated(this@ScanHighlightView)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        geometry = displayGeometry()
        displayManager?.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
    }

    override fun onDetachedFromWindow() {
        displayManager?.unregisterDisplayListener(displayListener)
        interval = null
        super.onDetachedFromWindow()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onDisplayInvalidated(this@ScanHighlightView)
    }

    var highlightBounds = ScanHighlightBounds(0f, 0f, 0f, 0f)
        set(value) { field = value; invalidate() }
    var highlightDrawable: Drawable? = null
        set(value) { field = value; invalidate() }
    var spotlight = false
        set(value) { field = value; invalidate() }
    var interval: ScanInterval? = null
        set(value) { field = value; invalidate() }
    var progressColor = Color.WHITE
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outline = Path()
    private val segment = Path()
    private val mask = Path()
    private val measure = PathMeasure()
    private val target = RectF()

    init {
        setWillNotDraw(false)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        isClickable = false
        isFocusable = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bounds = highlightBounds
        if (!bounds.isUsable) return
        target.set(bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height)
        val radius = min(ScanVisualConstants.CORNER_RADIUS_DP * density,
            min(bounds.width, bounds.height) / 2f)
        if (spotlight) {
            mask.reset()
            mask.fillType = Path.FillType.EVEN_ODD
            mask.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
            mask.addRoundRect(target, radius, radius, Path.Direction.CW)
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(ScanVisualConstants.SPOTLIGHT_ALPHA, 0, 0, 0)
            canvas.drawPath(mask, paint)
        }
        canvas.save()
        canvas.translate(bounds.x, bounds.y)
        highlightDrawable?.apply {
            setBounds(0, 0, bounds.width.roundToInt(), bounds.height.roundToInt())
            draw(canvas)
        }
        canvas.restore()
        val timing = interval ?: return
        val remaining = timing.remainingFraction(SystemClock.uptimeMillis())
        if (remaining <= 0f) return
        target.inset(-5f * density, -5f * density)
        perimeter(target, radius + 5f * density)
        measure.setPath(outline, false)
        segment.reset()
        measure.getSegment(measure.length * (1f - remaining), measure.length, segment, true)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 4f * density
        paint.color = if (progressColor == Color.WHITE) Color.BLACK else Color.WHITE
        canvas.drawPath(segment, paint)
        paint.strokeWidth = 2f * density
        paint.color = progressColor
        canvas.drawPath(segment, paint)
        postInvalidateOnAnimation()
    }

    private fun perimeter(rect: RectF, radius: Float) {
        val r = min(radius, min(rect.width(), rect.height()) / 2f)
        outline.reset()
        outline.moveTo(rect.centerX(), rect.top)
        outline.lineTo(rect.right - r, rect.top)
        outline.arcTo(rect.right - 2 * r, rect.top, rect.right, rect.top + 2 * r, -90f, 90f, false)
        outline.lineTo(rect.right, rect.bottom - r)
        outline.arcTo(rect.right - 2 * r, rect.bottom - 2 * r, rect.right, rect.bottom, 0f, 90f, false)
        outline.lineTo(rect.left + r, rect.bottom)
        outline.arcTo(rect.left, rect.bottom - 2 * r, rect.left + 2 * r, rect.bottom, 90f, 90f, false)
        outline.lineTo(rect.left, rect.top + r)
        outline.arcTo(rect.left, rect.top, rect.left + 2 * r, rect.top + 2 * r, 180f, 90f, false)
        outline.close()
    }
}
