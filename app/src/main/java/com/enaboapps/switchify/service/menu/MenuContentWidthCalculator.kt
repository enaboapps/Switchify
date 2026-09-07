package com.enaboapps.switchify.service.menu

internal data class MenuRowWidth(
    val labelWidthPx: Int,
    val fixedWidthPx: Int
)

internal object MenuContentWidthCalculator {
    fun calculate(
        maxWidthPx: Int,
        minimumWidthPx: Int,
        rows: List<MenuRowWidth>
    ): Int {
        val widestRow = rows.maxOfOrNull { row ->
            row.labelWidthPx.coerceAtLeast(0) + row.fixedWidthPx.coerceAtLeast(0)
        } ?: 0
        val requiredWidth = maxOf(minimumWidthPx.coerceAtLeast(0), widestRow)
        return requiredWidth.coerceIn(0, maxWidthPx.coerceAtLeast(0))
    }

    fun navigationCellWidth(
        availableWidthPx: Int,
        preferredWidthPx: Int,
        minimumTouchWidthPx: Int,
        itemCount: Int
    ): Int {
        if (itemCount <= 0) return 0
        val availablePerItem = availableWidthPx.coerceAtLeast(0) / itemCount
        val minimumWidth = minimumTouchWidthPx.coerceAtLeast(0).coerceAtMost(availablePerItem)
        return preferredWidthPx.coerceAtLeast(minimumWidth).coerceAtMost(availablePerItem)
    }
}

internal object MenuHorizontalPositionCalculator {
    fun clamp(preferredX: Int, menuWidth: Int, screenWidth: Int): Int {
        val maximumX = (screenWidth.coerceAtLeast(0) - menuWidth.coerceAtLeast(0)).coerceAtLeast(0)
        return preferredX.coerceIn(0, maximumX)
    }
}

internal object MenuVerticalPositionCalculator {
    /**
     * Clamps the menu's vertical position to the screen, then to the region below the
     * MenuHighlightHud. The top clamp is applied last so a menu taller than the space
     * between the HUD and the bottom of the screen stays under the HUD rather than
     * overflowing off the bottom.
     */
    fun clamp(preferredY: Int, menuHeight: Int, screenHeight: Int, topReserved: Int): Int {
        val safeHeight = menuHeight.coerceAtLeast(0)
        val safeTopReserved = topReserved.coerceAtLeast(0)
        val maximumY = (screenHeight.coerceAtLeast(0) - safeHeight).coerceAtLeast(0)
        return preferredY.coerceAtMost(maximumY).coerceAtLeast(safeTopReserved)
    }
}
