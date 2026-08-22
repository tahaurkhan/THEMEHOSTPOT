package com.example.themehostpot.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.themehostpot.R
import com.example.themehostpot.model.Hotspot
import com.example.themehostpot.model.HotspotWithApp

/**
 * Fullscreen Interactive Canvas component that scales any background wallpaper (9:16 aspect ratio)
 * and renders interactive hotspot zones based on normalized percentage coordinates.
 */
@Composable
fun InteractiveCanvas(
    hotspotsWithApps: List<HotspotWithApp>,
    isEditMode: Boolean,
    showDebugOutlines: Boolean,
    onCanvasTap: (xRatio: Float, yRatio: Float) -> Unit,
    onHotspotTap: (Hotspot) -> Unit,
    onHotspotLongPress: (Hotspot) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onTap = { offset ->
                        val xRatio = (offset.x / size.width).coerceIn(0f, 1f)
                        val yRatio = (offset.y / size.height).coerceIn(0f, 1f)
                        onCanvasTap(xRatio, yRatio)
                    }
                )
            }
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        val density = LocalDensity.current

        // 1. Background Wallpaper Image
        Image(
            painter = painterResource(id = R.drawable.cozy_desk_wallpaper),
            contentDescription = "Interactive Cozy Room Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Render Overlay Hotspots
        hotspotsWithApps.forEach { item ->
            val hotspot = item.hotspot
            val appInfo = item.appInfo

            val leftDp = with(density) { (hotspot.leftRatio * containerWidthPx).toDp() }
            val topDp = with(density) { (hotspot.topRatio * containerHeightPx).toDp() }
            val widthDp = with(density) { (hotspot.widthRatio * containerWidthPx).toDp() }
            val heightDp = with(density) { (hotspot.heightRatio * containerHeightPx).toDp() }

            HotspotOverlayZone(
                hotspot = hotspot,
                appIcon = appInfo?.iconDrawable,
                appLabel = appInfo?.label ?: hotspot.label,
                isEditMode = isEditMode,
                showDebugOutlines = showDebugOutlines,
                leftDp = leftDp,
                topDp = topDp,
                widthDp = widthDp,
                heightDp = heightDp,
                onTap = { onHotspotTap(hotspot) },
                onLongPress = { onHotspotLongPress(hotspot) }
            )
        }
    }
}

@Composable
private fun HotspotOverlayZone(
    hotspot: Hotspot,
    appIcon: Drawable?,
    appLabel: String,
    isEditMode: Boolean,
    showDebugOutlines: Boolean,
    leftDp: Dp,
    topDp: Dp,
    widthDp: Dp,
    heightDp: Dp,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    var isHoveredOrTapped by remember { mutableStateOf(false) }

    val outlineAlpha by animateFloatAsState(
        targetValue = if (isEditMode || showDebugOutlines || isHoveredOrTapped) 0.85f else 0.0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "outlineAlpha"
    )

    val boxColor = if (isEditMode) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = if (isHoveredOrTapped) 0.3f else 0.08f)
    }

    val borderColor = if (isEditMode) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = Modifier
            .offset(x = leftDp, y = topDp)
            .width(widthDp)
            .height(heightDp)
            .clip(RoundedCornerShape(12.dp))
            .background(boxColor.copy(alpha = boxColor.alpha * outlineAlpha))
            .drawBehind {
                if (outlineAlpha > 0.01f) {
                    val strokeWidth = 2.dp.toPx()
                    val stroke = if (isEditMode) {
                        Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    } else {
                        Stroke(width = strokeWidth)
                    }
                    drawRoundRect(
                        color = borderColor.copy(alpha = outlineAlpha),
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        style = stroke
                    )
                }
            }
            .combinedClickable(
                onClick = {
                    isHoveredOrTapped = true
                    onTap()
                },
                onLongClick = {
                    isHoveredOrTapped = true
                    onLongPress()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isEditMode || showDebugOutlines,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.padding(4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    if (appIcon != null) {
                        Image(
                            bitmap = appIcon.toBitmap(48, 48).asImageBitmap(),
                            contentDescription = appLabel,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = appLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
