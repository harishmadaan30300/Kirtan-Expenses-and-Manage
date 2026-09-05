package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE kirtanId = :kirtanId ORDER BY eventDateMillis DESC, id DESC")
    fun getBookingsForKirtan(kirtanId: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY eventDateMillis DESC, id DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY eventDateMillis DESC, id DESC")
    suspend fun getAllBookingsSync(): List<BookingEntity>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): BookingEntity?

    @Query("SELECT * FROM bookings WHERE categoryId = :categoryId ORDER BY eventDateMillis DESC, id DESC")
    fun getBookingsByCategory(categoryId: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookings(bookings: List<BookingEntity>): List<Long>

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBookingById(id: Long)

    @Query("DELETE FROM bookings WHERE kirtanId = :kirtanId")
    suspend fun deleteBookingsForKirtan(kirtanId: Long)

    @Query("DELETE FROM bookings")
    suspend fun deleteAllBookings()
}
