package com.enaboapps.switchify.service.scanning

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class ScanBorder(borderColor: String) : GradientDrawable() {

    init {
        shape = RECTANGLE
        setColor(Color.TRANSPARENT) // Set the fill color to transparent
        setStroke(4, Color.parseColor(borderColor)) // Set the border color and width
        cornerRadius = 16f // Set the corner radius
    }
}