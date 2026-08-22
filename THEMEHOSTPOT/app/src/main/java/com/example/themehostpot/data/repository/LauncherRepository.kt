package com.example.themehostpot.data.repository

import com.example.themehostpot.model.AppInfo
import com.example.themehostpot.model.Hotspot
import com.example.themehostpot.model.HotspotWithApp
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for managing launcher hotspots and package manager app queries.
 */
interface LauncherRepository {
    fun getHotspots(): Flow<List<Hotspot>>
    fun getHotspotsWithApps(): Flow<List<HotspotWithApp>>
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun saveHotspot(hotspot: Hotspot): Long
    suspend fun updateHotspot(hotspot: Hotspot)
    suspend fun deleteHotspot(hotspotId: Long)
    suspend fun resetDefaultTheme()
    fun launchApp(packageName: String): Result<Boolean>
}
