package intelbras.mobi.smart.business.streaming

import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageReading
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageResult

internal class StreamingMonitorImpl(
    private val streamingUsageReading: StreamingUsageReading,
) : StreamingMonitor {

    override suspend fun usageOf(sessionId: String): StreamingUsageResult =
        streamingUsageReading(sessionId)
}
