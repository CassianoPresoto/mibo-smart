package intelbras.mobi.smart.domain.streaming

import intelbras.mobi.smart.domain.streaming.model.StreamingQuota
import intelbras.mobi.smart.domain.streaming.model.StreamingSession
import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference

interface StreamingRepository {
    suspend fun readAvailableQuota(): StreamingQuota

    suspend fun listSessions(): List<StreamingSession>

    suspend fun readSession(reference: StreamingSessionReference): StreamingSession

    suspend fun endSession(reference: StreamingSessionReference)
}
