package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "donations",
    indices = [Index(value = ["kirtanId"])]
)
data class DonationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kirtanId: Long,
    val donorName: String,
    val amount: Double,
    val mobileNumber: String = "",
    val paymentMode: String = "CASH", // "CASH" or "UPI"
    val referenceId: String = "",     // UPI txn reference or slip number
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val receivedBy: String = "",      // Volunteer or sevak name
    val isPaymentReceived: Boolean = true // Tick option: payment received vs not received
)
