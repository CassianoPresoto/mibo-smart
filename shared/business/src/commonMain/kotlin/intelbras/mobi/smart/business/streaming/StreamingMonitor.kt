package intelbras.mobi.smart.business.streaming

import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageResult

interface StreamingMonitor {
    suspend fun usageOf(sessionId: String): StreamingUsageResult
}
