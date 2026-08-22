package com.example.themehostpot.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.themehostpot.ui.components.AppPickerSheet
import com.example.themehostpot.ui.components.InteractiveCanvas
import com.example.themehostpot.ui.state.HomeUiEvent
import com.example.themehostpot.ui.state.UiEffect
import com.example.themehostpot.ui.viewmodel.HomeViewModel

/**
 * Main Home Launcher Screen assembling the interactive background canvas,
 * floating controls, hotspot editor dialogs, and Material 3 App Picker sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showMenu by remember { mutableStateOf(false) }

    // Listen for one-shot UI effects (Toasts, App Launch events)
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is UiEffect.AppLaunched -> {
                    // Optional feedback on launching app
                }
            }
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Fullscreen Interactive Wallpaper Canvas
            InteractiveCanvas(
                hotspotsWithApps = uiState.hotspotsWithApps,
                isEditMode = uiState.isEditMode,
                showDebugOutlines = uiState.showDebugOutlines,
                onCanvasTap = { xRatio, yRatio ->
                    viewModel.onEvent(HomeUiEvent.OnCanvasTap(xRatio, yRatio))
                },
                onHotspotTap = { hotspot ->
                    viewModel.onEvent(HomeUiEvent.OnHotspotTap(hotspot))
                },
                onHotspotLongPress = { hotspot ->
                    viewModel.onEvent(HomeUiEvent.OnHotspotLongPress(hotspot))
                }
            )

            // 2. Floating Top Header & Action Controls Bar
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MapLauncher",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.isEditMode) "Edit Mode Active" else "Cozy Desk Theme",
                            fontSize = 11.sp,
                            color = if (uiState.isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Open All Apps Drawer Sheet
                        IconButton(
                            onClick = { viewModel.openAppPickerSheet() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "All Apps",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Toggle Debug Outlines
                        IconButton(
                            onClick = { viewModel.onEvent(HomeUiEvent.ToggleDebugOutlines) }
                        ) {
                            Icon(
                                imageVector = if (uiState.showDebugOutlines) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Outlines",
                                tint = if (uiState.showDebugOutlines) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Toggle Edit Mode FAB
                        FilledIconButton(
                            onClick = { viewModel.onEvent(HomeUiEvent.ToggleEditMode) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (uiState.isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Toggle Edit Mode",
                                tint = if (uiState.isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Dropdown Options Menu
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Reset to Default Theme") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.onEvent(HomeUiEvent.ResetDefaultTheme)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Floating Bottom Banner in Edit Mode
            AnimatedVisibility(
                visible = uiState.isEditMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap any object on wallpaper to edit or create hotspot",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 4. Hotspot Editor Dialog
            if (uiState.showEditorDialog && uiState.selectedHotspotForEdit != null) {
                HotspotEditorDialog(
                    hotspot = uiState.selectedHotspotForEdit!!,
                    onOpenAppPicker = { viewModel.openAppPickerSheet() },
                    onSaveHotspot = { updated ->
                        viewModel.onEvent(HomeUiEvent.SaveHotspot(updated))
                    },
                    onDeleteHotspot = { id ->
                        viewModel.onEvent(HomeUiEvent.DeleteHotspot(id))
                    },
                    onDismiss = {
                        viewModel.onEvent(HomeUiEvent.DismissEditorDialog)
                    }
                )
            }

            // 5. App Picker Sheet
            if (uiState.showAppPickerSheet) {
                AppPickerSheet(
                    installedApps = uiState.filteredApps,
                    searchQuery = uiState.searchQuery,
                    sheetState = sheetState,
                    onSearchQueryChange = { query ->
                        viewModel.onEvent(HomeUiEvent.SearchApps(query))
                    },
                    onAppSelected = { selectedApp ->
                        viewModel.onEvent(HomeUiEvent.SelectAppForHotspot(selectedApp))
                    },
                    onDismiss = {
                        viewModel.onEvent(HomeUiEvent.DismissAppPicker)
                    }
                )
            }
        }
    }
}
