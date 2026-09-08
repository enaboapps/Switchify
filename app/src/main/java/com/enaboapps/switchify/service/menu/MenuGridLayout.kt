package com.enaboapps.switchify.service.menu

internal data class MenuGridLayout(
    val columns: Int,
    val rows: Int,
    val cellWidthPx: Int,
    val cellHeightPx: Int
) {
    val pageCapacity: Int get() = columns * rows

    fun <T> pages(items: List<T>): List<List<T>> =
        items.chunked(pageCapacity).ifEmpty { listOf(emptyList()) }

    fun <T> rows(items: List<T>): List<List<T>> = items.chunked(columns)

    fun clampPage(page: Int, itemCount: Int): Int =
        page.coerceIn(0, ((itemCount - 1).coerceAtLeast(0) / pageCapacity))
}

internal object MenuGridLayoutPolicy {
    fun calculate(
        widthPx: Int,
        heightPx: Int,
        itemCount: Int,
        minimumCellWidthPx: Int,
        preferredCellHeightPx: Int,
        minimumTouchPx: Int,
        labelsFit: (cellWidthPx: Int) -> Boolean
    ): MenuGridLayout {
        val width = widthPx.coerceAtLeast(1)
        val minimumWidth = maxOf(minimumCellWidthPx, minimumTouchPx, 1)
        var columns = minOf(3, itemCount.coerceAtLeast(1), (width / minimumWidth).coerceAtLeast(1))
        while (columns > 1 && !labelsFit(width / columns)) columns--
        val cellHeight = preferredCellHeightPx.coerceAtLeast(minimumTouchPx)
            .coerceAtMost(heightPx.coerceAtLeast(minimumTouchPx))
        val rows = (heightPx / cellHeight.coerceAtLeast(1)).coerceAtLeast(1)
        return MenuGridLayout(columns, rows, width / columns, cellHeight)
    }
}

internal data class MenuPageSections<T>(
    val contextual: List<T>,
    val content: List<T>,
    val navigationSlots: List<T?>
) {
    fun rows(columns: Int, navigationColumns: Int): List<List<T>> =
        contextual.map { listOf(it) } + content.chunked(columns) +
            navigationSlots.chunked(navigationColumns).map { it.filterNotNull() }.filter { it.isNotEmpty() }
}
