package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.KirtanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KirtanDao {
    @Query("SELECT * FROM kirtans ORDER BY dateMillis DESC")
    fun getAllKirtans(): Flow<List<KirtanEntity>>

    @Query("SELECT * FROM kirtans WHERE id = :id LIMIT 1")
    fun getKirtanById(id: Long): Flow<KirtanEntity?>

    @Query("SELECT * FROM kirtans ORDER BY dateMillis DESC")
    suspend fun getAllKirtansSync(): List<KirtanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKirtan(kirtan: KirtanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKirtans(kirtans: List<KirtanEntity>): List<Long>

    @Update
    suspend fun updateKirtan(kirtan: KirtanEntity)

    @Delete
    suspend fun deleteKirtan(kirtan: KirtanEntity)

    @Query("DELETE FROM kirtans WHERE id = :id")
    suspend fun deleteKirtanById(id: Long)

    @Query("DELETE FROM kirtans")
    suspend fun deleteAllKirtans()
}
