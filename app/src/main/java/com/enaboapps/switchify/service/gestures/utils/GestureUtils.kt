package com.enaboapps.switchify.service.gestures.utils

object GestureUtils {

    /**
     * Get a coordinate that is within the bounds of the screen
     * @param widthOrHeight The width or height of the screen
     * @param target The target coordinate
     * @return The coordinate within the bounds
     */
    fun getInBoundsCoordinate(widthOrHeight: Int, target: Float): Float {
        return if (target <= 0) {
            0f
        } else if (target >= widthOrHeight) {
            widthOrHeight.toFloat()
        } else {
            target
        }
    }

}