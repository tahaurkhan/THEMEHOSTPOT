package com.example.themehostpot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.themehostpot.data.repository.LauncherRepository
import com.example.themehostpot.model.AppInfo
import com.example.themehostpot.model.Hotspot
import com.example.themehostpot.ui.state.HomeUiEvent
import com.example.themehostpot.ui.state.HomeUiState
import com.example.themehostpot.ui.state.UiEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: LauncherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UiEffect>()
    val uiEffect: SharedFlow<UiEffect> = _uiEffect.asSharedFlow()

    init {
        observeHotspots()
        loadInstalledApps()
    }

    private fun observeHotspots() {
        viewModelScope.launch {
            repository.getHotspotsWithApps().collect { hotspotsWithApps ->
                _uiState.update { state ->
                    state.copy(
                        hotspotsWithApps = hotspotsWithApps,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = repository.getInstalledApps()
            _uiState.update { state ->
                state.copy(
                    installedApps = apps,
                    filteredApps = filterAppsList(apps, state.searchQuery)
                )
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnCanvasTap -> handleCanvasTap(event.xRatio, event.yRatio)
            is HomeUiEvent.OnHotspotTap -> launchHotspotApp(event.hotspot)
            is HomeUiEvent.OnHotspotLongPress -> openEditorForHotspot(event.hotspot)
            is HomeUiEvent.ToggleEditMode -> toggleEditMode()
            is HomeUiEvent.ToggleDebugOutlines -> toggleDebugOutlines()
            is HomeUiEvent.OpenEditorForNew -> openEditorForNewPoint(event.xRatio, event.yRatio)
            is HomeUiEvent.OpenEditorForEdit -> openEditorForHotspot(event.hotspot)
            is HomeUiEvent.SearchApps -> searchApps(event.query)
            is HomeUiEvent.SelectAppForHotspot -> selectAppForHotspot(event.appInfo)
            is HomeUiEvent.SaveHotspot -> saveHotspot(event.hotspot)
            is HomeUiEvent.DeleteHotspot -> deleteHotspot(event.hotspotId)
            is HomeUiEvent.ResetDefaultTheme -> resetDefaultTheme()
            is HomeUiEvent.DismissAppPicker -> dismissAppPicker()
            is HomeUiEvent.DismissEditorDialog -> dismissEditorDialog()
        }
    }

    private fun handleCanvasTap(xRatio: Float, yRatio: Float) {
        val currentState = _uiState.value

        // Check if tap landed inside any existing hotspot
        val tappedHotspotWithApp = currentState.hotspotsWithApps.firstOrNull {
            it.hotspot.containsPoint(xRatio, yRatio)
        }

        if (tappedHotspotWithApp != null) {
            if (currentState.isEditMode) {
                openEditorForHotspot(tappedHotspotWithApp.hotspot)
            } else {
                launchHotspotApp(tappedHotspotWithApp.hotspot)
            }
        } else {
            if (currentState.isEditMode) {
                openEditorForNewPoint(xRatio, yRatio)
            }
        }
    }

    private fun launchHotspotApp(hotspot: Hotspot) {
        val result = repository.launchApp(hotspot.packageName)
        viewModelScope.launch {
            if (result.isSuccess) {
                _uiEffect.emit(UiEffect.AppLaunched(hotspot.label))
            } else {
                _uiEffect.emit(
                    UiEffect.ShowToast(
                        "Could not launch app for ${hotspot.label}. Opening picker..."
                    )
                )
                openEditorForHotspot(hotspot)
            }
        }
    }

    private fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    private fun toggleDebugOutlines() {
        _uiState.update { it.copy(showDebugOutlines = !it.showDebugOutlines) }
    }

    private fun openEditorForNewPoint(xRatio: Float, yRatio: Float) {
        // Default box size around tap point
        val defaultWidth = 0.15f
        val defaultHeight = 0.12f
        val left = (xRatio - defaultWidth / 2f).coerceIn(0f, 1f - defaultWidth)
        val top = (yRatio - defaultHeight / 2f).coerceIn(0f, 1f - defaultHeight)

        val newHotspot = Hotspot(
            id = 0,
            label = "New Hotspot",
            packageName = "",
            leftRatio = left,
            topRatio = top,
            widthRatio = defaultWidth,
            heightRatio = defaultHeight
        )

        _uiState.update {
            it.copy(
                selectedHotspotForEdit = newHotspot,
                pendingClickPoint = Pair(xRatio, yRatio),
                showEditorDialog = true
            )
        }
    }

    private fun openEditorForHotspot(hotspot: Hotspot) {
        _uiState.update {
            it.copy(
                selectedHotspotForEdit = hotspot,
                showEditorDialog = true
            )
        }
    }

    private fun searchApps(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredApps = filterAppsList(state.installedApps, query)
            )
        }
    }

    private fun filterAppsList(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return apps
        return apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
        }
    }

    private fun selectAppForHotspot(appInfo: AppInfo) {
        val currentHotspot = _uiState.value.selectedHotspotForEdit ?: return
        val updatedHotspot = currentHotspot.copy(
            label = appInfo.label,
            packageName = appInfo.packageName
        )

        _uiState.update {
            it.copy(
                selectedHotspotForEdit = updatedHotspot,
                showAppPickerSheet = false
            )
        }
    }

    private fun saveHotspot(hotspot: Hotspot) {
        viewModelScope.launch {
            if (hotspot.id == 0L) {
                repository.saveHotspot(hotspot)
            } else {
                repository.updateHotspot(hotspot)
            }
            _uiState.update {
                it.copy(
                    showEditorDialog = false,
                    selectedHotspotForEdit = null
                )
            }
            _uiEffect.emit(UiEffect.ShowToast("Hotspot saved: ${hotspot.label}"))
        }
    }

    private fun deleteHotspot(hotspotId: Long) {
        viewModelScope.launch {
            repository.deleteHotspot(hotspotId)
            _uiState.update {
                it.copy(
                    showEditorDialog = false,
                    selectedHotspotForEdit = null
                )
            }
            _uiEffect.emit(UiEffect.ShowToast("Hotspot removed"))
        }
    }

    private fun resetDefaultTheme() {
        viewModelScope.launch {
            repository.resetDefaultTheme()
            _uiEffect.emit(UiEffect.ShowToast("Reset to default Cozy Desk Theme!"))
        }
    }

    private fun dismissAppPicker() {
        _uiState.update { it.copy(showAppPickerSheet = false) }
    }

    private fun dismissEditorDialog() {
        _uiState.update {
            it.copy(
                showEditorDialog = false,
                selectedHotspotForEdit = null
            )
        }
    }

    fun openAppPickerSheet() {
        _uiState.update { it.copy(showAppPickerSheet = true) }
    }
}

class HomeViewModelFactory(
    private val repository: LauncherRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
