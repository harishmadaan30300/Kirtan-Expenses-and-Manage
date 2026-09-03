package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["kirtanId"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kirtanId: Long,
    val title: String,
    val category: String = "Miscellaneous",
    val amount: Double,
    val paymentMode: String = "CASH", // "CASH" or "UPI"
    val paidTo: String = "",          // Vendor or payee name
    val referenceId: String = "",     // UPI txn id or receipt number
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
