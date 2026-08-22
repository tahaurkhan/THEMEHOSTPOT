package com.example.themehostpot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HotspotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hotspotDao(): HotspotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "map_launcher_db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-load default cozy desk theme hotspots
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultThemeHotspots(database.hotspotDao())
                }
            }
        }
    }
}

/**
 * Seed default theme hotspots corresponding to physical objects in the cozy desk room wallpaper.
 */
suspend fun populateDefaultThemeHotspots(dao: HotspotDao) {
    if (dao.getHotspotCount() == 0) {
        val defaultHotspots = listOf(
            HotspotEntity(
                label = "Camera",
                packageName = "com.android.camera",
                leftRatio = 0.60f,
                topRatio = 0.16f,
                widthRatio = 0.16f,
                heightRatio = 0.12f,
                iconResName = "ic_camera"
            ),
            HotspotEntity(
                label = "Laptop (Browser)",
                packageName = "com.android.chrome",
                leftRatio = 0.40f,
                topRatio = 0.33f,
                widthRatio = 0.30f,
                heightRatio = 0.25f,
                iconResName = "ic_laptop"
            ),
            HotspotEntity(
                label = "Retro Radio (Music)",
                packageName = "com.spotify.music",
                leftRatio = 0.78f,
                topRatio = 0.58f,
                widthRatio = 0.20f,
                heightRatio = 0.18f,
                iconResName = "ic_radio"
            ),
            HotspotEntity(
                label = "Desk Clock",
                packageName = "com.google.android.deskclock",
                leftRatio = 0.72f,
                topRatio = 0.50f,
                widthRatio = 0.12f,
                heightRatio = 0.10f,
                iconResName = "ic_clock"
            ),
            HotspotEntity(
                label = "Coffee & Notebook",
                packageName = "com.google.android.keep",
                leftRatio = 0.36f,
                topRatio = 0.40f,
                widthRatio = 0.12f,
                heightRatio = 0.12f,
                iconResName = "ic_notes"
            ),
            HotspotEntity(
                label = "Sealed Envelope (Messages)",
                packageName = "com.google.android.apps.messaging",
                leftRatio = 0.29f,
                topRatio = 0.46f,
                widthRatio = 0.12f,
                heightRatio = 0.08f,
                iconResName = "ic_mail"
            ),
            HotspotEntity(
                label = "Succulent Plant (Gallery)",
                packageName = "com.google.android.apps.photos",
                leftRatio = 0.42f,
                topRatio = 0.30f,
                widthRatio = 0.10f,
                heightRatio = 0.14f,
                iconResName = "ic_photos"
            )
        )
        dao.insertAll(defaultHotspots)
    }
}
