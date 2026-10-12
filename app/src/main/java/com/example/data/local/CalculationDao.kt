package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(entity: CalculationEntity): Long

    @Delete
    suspend fun deleteCalculation(entity: CalculationEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()

    @Query("DELETE FROM calculation_history WHERE id NOT IN (SELECT id FROM calculation_history ORDER BY timestamp DESC LIMIT 200)")
    suspend fun trimOldHistory()
}
