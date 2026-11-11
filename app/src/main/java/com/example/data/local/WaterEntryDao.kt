package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WaterEntry): Long

    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT * FROM water_entries WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun getEntriesByDate(dateKey: String): Flow<List<WaterEntry>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_entries WHERE dateKey = :dateKey")
    fun getDailyIntakeMl(dateKey: String): Flow<Int>

    @Query("SELECT * FROM water_entries WHERE timestamp >= :startTimeMillis ORDER BY timestamp DESC")
    fun getEntriesSince(startTimeMillis: Long): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<WaterEntry>>

    @Query("SELECT dateKey, SUM(amountMl) as totalMl FROM water_entries GROUP BY dateKey ORDER BY dateKey DESC")
    fun getDailyTotals(): Flow<List<DateTotal>>

    @Query("SELECT MAX(timestamp) FROM water_entries")
    suspend fun getLatestEntryTimestamp(): Long?

    @Query("DELETE FROM water_entries")
    suspend fun clearAll()
}

data class DateTotal(
    val dateKey: String,
    val totalMl: Int
)
