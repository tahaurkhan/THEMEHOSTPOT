package com.example.themehostpot.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.themehostpot.model.Hotspot

/**
 * Visual Hotspot Editor Dialog allowing exact coordinate tweaking and app selection.
 */
@Composable
fun HotspotEditorDialog(
    hotspot: Hotspot,
    onOpenAppPicker: () -> Unit,
    onSaveHotspot: (Hotspot) -> Unit,
    onDeleteHotspot: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember(hotspot) { mutableStateOf(hotspot.label) }
    var packageName by remember(hotspot) { mutableStateOf(hotspot.packageName) }
    var leftRatio by remember(hotspot) { mutableFloatStateOf(hotspot.leftRatio) }
    var topRatio by remember(hotspot) { mutableFloatStateOf(hotspot.topRatio) }
    var widthRatio by remember(hotspot) { mutableFloatStateOf(hotspot.widthRatio) }
    var heightRatio by remember(hotspot) { mutableFloatStateOf(hotspot.heightRatio) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (hotspot.id == 0L) "New Hotspot" else "Configure Hotspot",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Label Input
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Hotspot Name / Label") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Package / App Selector Button
                Card(
                    onClick = onOpenAppPicker,
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Target Application",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (packageName.isNotBlank()) packageName else "Tap to choose app...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Pick App",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hotspot Region Geometry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Slider: Left Position
                CoordinateSlider(
                    label = "Horizontal Offset (X)",
                    value = leftRatio,
                    onValueChange = { leftRatio = it.coerceIn(0f, 1f - widthRatio) },
                    valueText = "${(leftRatio * 100).toInt()}%"
                )

                // Slider: Top Position
                CoordinateSlider(
                    label = "Vertical Offset (Y)",
                    value = topRatio,
                    onValueChange = { topRatio = it.coerceIn(0f, 1f - heightRatio) },
                    valueText = "${(topRatio * 100).toInt()}%"
                )

                // Slider: Width Ratio
                CoordinateSlider(
                    label = "Zone Width",
                    value = widthRatio,
                    onValueChange = { widthRatio = it.coerceIn(0.05f, 0.50f) },
                    valueText = "${(widthRatio * 100).toInt()}%"
                )

                // Slider: Height Ratio
                CoordinateSlider(
                    label = "Zone Height",
                    value = heightRatio,
                    onValueChange = { heightRatio = it.coerceIn(0.05f, 0.50f) },
                    valueText = "${(heightRatio * 100).toInt()}%"
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hotspot.id != 0L) {
                        OutlinedButton(
                            onClick = { onDeleteHotspot(hotspot.id) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val updated = hotspot.copy(
                                label = label.ifBlank { "Hotspot" },
                                packageName = packageName,
                                leftRatio = leftRatio,
                                topRatio = topRatio,
                                widthRatio = widthRatio,
                                heightRatio = heightRatio
                            )
                            onSaveHotspot(updated)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinateSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueText: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valueText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.0f..1.0f
        )
    }
}
