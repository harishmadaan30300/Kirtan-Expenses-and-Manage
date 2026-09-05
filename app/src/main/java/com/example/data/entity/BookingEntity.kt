package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kirtanId: Long? = null,
    val categoryId: String,          // e.g. "garden_hall", "singers", "musician", "sound", etc.
    val serviceTitle: String,        // e.g. "Garden/Hall", "Singers", etc.
    val vendorName: String,          // e.g. "श्री राधे गार्डन / मोहन साउंड"
    val contactNumber: String = "",  // e.g. "9876543210"
    val eventDateMillis: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val status: String = "CONFIRMED", // "CONFIRMED", "ADVANCE_PAID", "PENDING", "COMPLETED"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
