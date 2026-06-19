package com.example.db

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<MeasurementSession>> = sessionDao.getAllSessions()
    val recentlyDeletedSessions: Flow<List<MeasurementSession>> = sessionDao.getRecentlyDeletedSessions()

    fun getDatapointsForSession(sessionId: Long): Flow<List<MeasurementDatapoint>> {
        return sessionDao.getDatapointsForSession(sessionId)
    }

    suspend fun getDatapointsForSessionSync(sessionId: Long): List<MeasurementDatapoint> {
        return sessionDao.getDatapointsForSessionSync(sessionId)
    }

    suspend fun getSessionById(sessionId: Long): MeasurementSession? {
        return sessionDao.getSessionById(sessionId)
    }

    suspend fun insertSessionWithPoints(session: MeasurementSession, points: List<MeasurementDatapoint>): Long {
        val sessionId = sessionDao.insertSession(session)
        val updatedPoints = points.map { it.copy(sessionId = sessionId) }
        sessionDao.insertDatapoints(updatedPoints)
        return sessionId
    }

    suspend fun deleteSession(sessionId: Long) {
        sessionDao.markSessionAsDeleted(sessionId, System.currentTimeMillis())
    }

    suspend fun restoreSession(sessionId: Long) {
        sessionDao.markSessionAsDeleted(sessionId, null)
    }

    suspend fun deleteSessionPermanently(sessionId: Long) {
        sessionDao.deleteSessionByIdPermanent(sessionId)
    }

    suspend fun deleteAllPermanently() {
        sessionDao.deleteAllPermanently()
    }

    suspend fun purgeOldDeleted(cutOffTime: Long) {
        sessionDao.purgeOldDeletedSessions(cutOffTime)
    }

    suspend fun updateSessionTitle(sessionId: Long, newTitle: String) {
        sessionDao.updateSessionTitle(sessionId, newTitle)
    }
}
