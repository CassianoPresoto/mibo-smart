package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.StreamingUsageResult

interface StreamingMonitor {
    suspend fun usageOf(sessionId: String): StreamingUsageResult
}
