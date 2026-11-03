package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateKey: String
)
