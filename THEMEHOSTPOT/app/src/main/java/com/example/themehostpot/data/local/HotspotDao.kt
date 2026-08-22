package com.example.themehostpot.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local Hotspot operations.
 */
@Dao
interface HotspotDao {

    @Query("SELECT * FROM hotspots ORDER BY id ASC")
    fun getAllHotspots(): Flow<List<HotspotEntity>>

    @Query("SELECT * FROM hotspots WHERE id = :id LIMIT 1")
    suspend fun getHotspotById(id: Long): HotspotEntity?

    @Query("SELECT COUNT(*) FROM hotspots")
    suspend fun getHotspotCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotspot(hotspot: HotspotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hotspots: List<HotspotEntity>)

    @Update
    suspend fun updateHotspot(hotspot: HotspotEntity)

    @Delete
    suspend fun deleteHotspot(hotspot: HotspotEntity)

    @Query("DELETE FROM hotspots WHERE id = :id")
    suspend fun deleteHotspotById(id: Long)

    @Query("DELETE FROM hotspots")
    suspend fun clearAll()
}
