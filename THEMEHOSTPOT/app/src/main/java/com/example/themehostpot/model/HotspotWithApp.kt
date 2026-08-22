package com.example.themehostpot.model

/**
 * Combines a interactive hotspot domain entity with its target application metadata.
 */
data class HotspotWithApp(
    val hotspot: Hotspot,
    val appInfo: AppInfo? = null
)
