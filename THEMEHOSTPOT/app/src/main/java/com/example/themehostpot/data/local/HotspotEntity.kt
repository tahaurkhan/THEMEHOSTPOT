package com.example.themehostpot.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.themehostpot.model.Hotspot

/**
 * Room entity table storing interactive hotspot configurations.
 */
@Entity(tableName = "hotspots")
data class HotspotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val packageName: String,
    val leftRatio: Float,
    val topRatio: Float,
    val widthRatio: Float,
    val heightRatio: Float,
    val iconResName: String? = null
)

fun HotspotEntity.toDomain(): Hotspot = Hotspot(
    id = id,
    label = label,
    packageName = packageName,
    leftRatio = leftRatio,
    topRatio = topRatio,
    widthRatio = widthRatio,
    heightRatio = heightRatio,
    iconResName = iconResName
)

fun Hotspot.toEntity(): HotspotEntity = HotspotEntity(
    id = id,
    label = label,
    packageName = packageName,
    leftRatio = leftRatio,
    topRatio = topRatio,
    widthRatio = widthRatio,
    heightRatio = heightRatio,
    iconResName = iconResName
)
