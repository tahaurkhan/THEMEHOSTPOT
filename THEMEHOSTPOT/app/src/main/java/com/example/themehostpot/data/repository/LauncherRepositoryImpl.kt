package com.example.themehostpot.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import com.example.themehostpot.data.local.HotspotDao
import com.example.themehostpot.data.local.populateDefaultThemeHotspots
import com.example.themehostpot.data.local.toEntity
import com.example.themehostpot.data.local.toDomain
import com.example.themehostpot.model.AppInfo
import com.example.themehostpot.model.Hotspot
import com.example.themehostpot.model.HotspotWithApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LauncherRepositoryImpl(
    private val context: Context,
    private val hotspotDao: HotspotDao
) : LauncherRepository {

    private val packageManager: PackageManager = context.packageManager

    override fun getHotspots(): Flow<List<Hotspot>> {
        return hotspotDao.getAllHotspots().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHotspotsWithApps(): Flow<List<HotspotWithApp>> {
        return getHotspots().map { hotspots ->
            val installedAppsMap = getInstalledAppsMap()
            hotspots.map { hotspot ->
                val appInfo = installedAppsMap[hotspot.packageName]
                    ?: resolveFallbackAppInfo(hotspot.packageName, hotspot.label)
                HotspotWithApp(hotspot = hotspot, appInfo = appInfo)
            }
        }
    }

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(
            mainIntent,
            PackageManager.MATCH_ALL
        )

        val selfPackageName = context.packageName

        resolveInfos
            .filter { it.activityInfo.packageName != selfPackageName }
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(packageManager).toString()
                val pkgName = resolveInfo.activityInfo.packageName
                val icon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }
                AppInfo(label = label, packageName = pkgName, iconDrawable = icon)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun getInstalledAppsMap(): Map<String, AppInfo> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(
            mainIntent,
            PackageManager.MATCH_ALL
        )

        return resolveInfos.associate { resolveInfo ->
            val label = resolveInfo.loadLabel(packageManager).toString()
            val pkgName = resolveInfo.activityInfo.packageName
            val icon = try {
                resolveInfo.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }
            pkgName to AppInfo(label = label, packageName = pkgName, iconDrawable = icon)
        }
    }

    private fun resolveFallbackAppInfo(packageName: String, fallbackLabel: String): AppInfo {
        val icon = try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
        return AppInfo(
            label = fallbackLabel,
            packageName = packageName,
            iconDrawable = icon
        )
    }

    override suspend fun saveHotspot(hotspot: Hotspot): Long = withContext(Dispatchers.IO) {
        hotspotDao.insertHotspot(hotspot.toEntity())
    }

    override suspend fun updateHotspot(hotspot: Hotspot) = withContext(Dispatchers.IO) {
        hotspotDao.updateHotspot(hotspot.toEntity())
    }

    override suspend fun deleteHotspot(hotspotId: Long) = withContext(Dispatchers.IO) {
        hotspotDao.deleteHotspotById(hotspotId)
    }

    override suspend fun resetDefaultTheme() = withContext(Dispatchers.IO) {
        hotspotDao.clearAll()
        populateDefaultThemeHotspots(hotspotDao)
    }

    override fun launchApp(packageName: String): Result<Boolean> {
        return try {
            // Attempt standard launcher intent
            var intent = packageManager.getLaunchIntentForPackage(packageName)

            // Fallbacks for standard system app intents if explicit package is missing
            if (intent == null) {
                intent = when {
                    packageName.contains("camera", ignoreCase = true) -> {
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    }
                    packageName.contains("chrome", ignoreCase = true) || packageName.contains("browser", ignoreCase = true) -> {
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                    }
                    packageName.contains("messaging", ignoreCase = true) || packageName.contains("sms", ignoreCase = true) -> {
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MESSAGING)
                        }
                    }
                    packageName.contains("deskclock", ignoreCase = true) || packageName.contains("clock", ignoreCase = true) -> {
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_CALCULATOR)
                        }
                    }
                    else -> null
                }
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Result.success(true)
            } else {
                Result.failure(Exception("No launchable application found for $packageName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
