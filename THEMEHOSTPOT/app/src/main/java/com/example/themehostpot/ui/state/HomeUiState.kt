package com.example.themehostpot.ui.state

import com.example.themehostpot.model.AppInfo
import com.example.themehostpot.model.Hotspot
import com.example.themehostpot.model.HotspotWithApp

data class HomeUiState(
    val hotspotsWithApps: List<HotspotWithApp> = emptyList(),
    val installedApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isEditMode: Boolean = false,
    val showAppPickerSheet: Boolean = false,
    val showEditorDialog: Boolean = false,
    val selectedHotspotForEdit: Hotspot? = null,
    val pendingClickPoint: Pair<Float, Float>? = null, // Normalized (xRatio, yRatio)
    val isLoading: Boolean = false,
    val showDebugOutlines: Boolean = true
)
