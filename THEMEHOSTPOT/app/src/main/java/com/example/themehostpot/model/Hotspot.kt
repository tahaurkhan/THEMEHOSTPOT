package com.example.themehostpot.model

/**
 * Domain model representing an interactive hotspot zone on the wallpaper.
 *
 * Coordinates are normalized float ratios between 0.0 and 1.0 relative to
 * the 9:16 background canvas.
 */
data class Hotspot(
    val id: Long = 0,
    val label: String,
    val packageName: String,
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
    val iconResName: String? = null
) {
    /**
     * Helper to check if a normalized point (xRatio, yRatio) falls within this hotspot.
     */
    fun containsPoint(xRatio: Float, yRatio: Float): Boolean {
        return xRatio >= leftRatio &&
                xRatio <= (leftRatio + widthRatio) &&
                yRatio >= topRatio &&
                yRatio <= (topRatio + heightRatio)
    }
}
