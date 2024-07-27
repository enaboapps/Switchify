package com.enaboapps.switchify.service.scanning

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class ScanHighlightDrawable(color: String) : GradientDrawable() {

    init {
        // Set the color of the drawable with alpha of 40%
        setColor(Color.parseColor(color))
        alpha = 102
        // Set the corner radius of the drawable
        cornerRadius = 16f
        // Set the stroke width of the drawable without alpha
        setStroke(8, Color.parseColor(color) and 0xFFFFFF)
    }
}