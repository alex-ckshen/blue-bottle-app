package com.example.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_sessions")
data class MeasurementSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val avgLux: Float = 0f,
    val maxLux: Float = 0f,
    val minLux: Float = 0f,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "measurement_datapoints",
    foreignKeys = [
        ForeignKey(
            entity = MeasurementSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class MeasurementDatapoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val timeMillis: Long, // timestamp of point
    val lux: Float
)
