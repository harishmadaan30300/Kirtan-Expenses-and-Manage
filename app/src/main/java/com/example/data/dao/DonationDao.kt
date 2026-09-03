package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DonationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations WHERE kirtanId = :kirtanId ORDER BY dateMillis DESC")
    fun getDonationsForKirtan(kirtanId: Long): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations ORDER BY dateMillis DESC")
    fun getAllDonations(): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations ORDER BY dateMillis DESC")
    suspend fun getAllDonationsSync(): List<DonationEntity>

    @Query("SELECT * FROM donations WHERE id = :id LIMIT 1")
    suspend fun getDonationById(id: Long): DonationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDonations(donations: List<DonationEntity>): List<Long>

    @Update
    suspend fun updateDonation(donation: DonationEntity)

    @Delete
    suspend fun deleteDonation(donation: DonationEntity)

    @Query("DELETE FROM donations WHERE id = :id")
    suspend fun deleteDonationById(id: Long)

    @Query("DELETE FROM donations WHERE kirtanId = :kirtanId")
    suspend fun deleteDonationsForKirtan(kirtanId: Long)

    @Query("DELETE FROM donations")
    suspend fun deleteAllDonations()
}
