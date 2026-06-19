package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM measurement_sessions WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<MeasurementSession>>

    @Query("SELECT * FROM measurement_sessions WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getRecentlyDeletedSessions(): Flow<List<MeasurementSession>>

    @Query("SELECT * FROM measurement_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): MeasurementSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MeasurementSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatapoints(points: List<MeasurementDatapoint>)

    @Query("SELECT * FROM measurement_datapoints WHERE sessionId = :sessionId ORDER BY timeMillis ASC")
    fun getDatapointsForSession(sessionId: Long): Flow<List<MeasurementDatapoint>>

    @Query("SELECT * FROM measurement_datapoints WHERE sessionId = :sessionId ORDER BY timeMillis ASC")
    suspend fun getDatapointsForSessionSync(sessionId: Long): List<MeasurementDatapoint>

    @Query("UPDATE measurement_sessions SET deletedAt = :deletedAt WHERE id = :sessionId")
    suspend fun markSessionAsDeleted(sessionId: Long, deletedAt: Long?)

    @Query("DELETE FROM measurement_sessions WHERE id = :sessionId")
    suspend fun deleteSessionByIdPermanent(sessionId: Long)

    @Query("DELETE FROM measurement_sessions WHERE deletedAt IS NOT NULL")
    suspend fun deleteAllPermanently()

    @Query("DELETE FROM measurement_sessions WHERE deletedAt IS NOT NULL AND deletedAt < :cutOffTime")
    suspend fun purgeOldDeletedSessions(cutOffTime: Long)

    @Query("UPDATE measurement_sessions SET title = :newTitle WHERE id = :sessionId")
    suspend fun updateSessionTitle(sessionId: Long, newTitle: String)
}
