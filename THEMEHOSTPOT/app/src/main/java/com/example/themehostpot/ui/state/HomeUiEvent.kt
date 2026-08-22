package com.example.themehostpot.ui.state

import com.example.themehostpot.model.AppInfo
import com.example.themehostpot.model.Hotspot

sealed interface HomeUiEvent {
    data class OnCanvasTap(val xRatio: Float, val yRatio: Float) : HomeUiEvent
    data class OnHotspotTap(val hotspot: Hotspot) : HomeUiEvent
    data class OnHotspotLongPress(val hotspot: Hotspot) : HomeUiEvent
    object ToggleEditMode : HomeUiEvent
    object ToggleDebugOutlines : HomeUiEvent
    data class OpenEditorForNew(val xRatio: Float, val yRatio: Float) : HomeUiEvent
    data class OpenEditorForEdit(val hotspot: Hotspot) : HomeUiEvent
    data class SearchApps(val query: String) : HomeUiEvent
    data class SelectAppForHotspot(val appInfo: AppInfo) : HomeUiEvent
    data class SaveHotspot(val hotspot: Hotspot) : HomeUiEvent
    data class DeleteHotspot(val hotspotId: Long) : HomeUiEvent
    object ResetDefaultTheme : HomeUiEvent
    object DismissAppPicker : HomeUiEvent
    object DismissEditorDialog : HomeUiEvent
}

sealed interface UiEffect {
    data class ShowToast(val message: String) : UiEffect
    data class AppLaunched(val label: String) : UiEffect
}
