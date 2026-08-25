package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.StreamingUsageReading
import intelbras.mobi.smart.business.usecase.StreamingUsageResult

internal class StreamingMonitorImpl(
    private val streamingUsageReading: StreamingUsageReading,
) : StreamingMonitor {

    override suspend fun usageOf(sessionId: String): StreamingUsageResult =
        streamingUsageReading(sessionId)
}
