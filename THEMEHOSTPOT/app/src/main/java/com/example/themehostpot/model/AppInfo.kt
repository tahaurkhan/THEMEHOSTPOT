package com.example.themehostpot.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed Android application.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val iconDrawable: Drawable? = null
)
