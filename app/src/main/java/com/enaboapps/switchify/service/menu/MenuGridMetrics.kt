package com.enaboapps.switchify.service.menu

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import kotlin.math.ceil

internal data class MenuGridMetrics(
    val grid: MenuGridLayout,
    val widthPx: Int,
    val titleHeightPx: Int,
    val contextualHeightPx: Int,
    val navigationColumns: Int,
    val navigationHeightPx: Int,
    val pageCountHeightPx: Int
)

internal object MenuGridMeasurer {
    fun measure(
        context: Context,
        items: List<MenuItem>,
        contextualCount: Int,
        hasTitle: Boolean,
        hasNavigation: Boolean
    ): MenuGridMetrics {
        val size = MenuSizeManager.getItemSize(context)
        fun dp(value: Float) = ceil(value * context.resources.displayMetrics.density).toInt()
        fun sp(value: Float) = ceil(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, value, context.resources.displayMetrics
        )).toInt()
        val width = MenuSurfaceBudget.contentMaxWidthPx(context).coerceAtLeast(1)
        val minimumTouch = dp(52f)
        val titleHeight = if (hasTitle) sp(24f) + dp(12f) else 0
        val pageCountHeight = sp(20f) + dp(8f)
        val navigationColumns = if (width >= 4 * maxOf(minimumTouch, dp(64f))) 4 else 2
        val navHeight = maxOf(minimumTouch, dp(28f) + sp(size.labelTextSize.value * 2.6f) + dp(12f))
        val contextualHeight = maxOf(minimumTouch, sp(size.labelTextSize.value * 2.6f) + dp(16f))
        val chrome = titleHeight + contextualCount * contextualHeight +
            if (hasNavigation) (4 / navigationColumns) * navHeight + dp(8f) else 0
        val bodyHeight = MenuSurfaceBudget.surfaceMaxHeightPx(context) - dp(36f) - chrome - pageCountHeight
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = sp(size.labelTextSize.value).toFloat()
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            letterSpacing = 0.5f / size.labelTextSize.value
        }
        val preferredHeight = dp(size.iconSize.value + 24f) + sp(size.labelTextSize.value * 3.9f)
        val grid = MenuGridLayoutPolicy.calculate(
            width, bodyHeight, items.size, dp(maxOf(80f, size.width.value)),
            preferredHeight, minimumTouch
        ) { cellWidth ->
            items.all { item ->
                val text = item.displayText().take(4096)
                val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint,
                    (cellWidth - dp(16f)).coerceAtLeast(1))
                    .setIncludePad(false).setMaxLines(4).build()
                layout.lineCount <= 3
            }
        }
        return MenuGridMetrics(grid, width, titleHeight, contextualHeight,
            navigationColumns, navHeight, pageCountHeight)
    }
}
