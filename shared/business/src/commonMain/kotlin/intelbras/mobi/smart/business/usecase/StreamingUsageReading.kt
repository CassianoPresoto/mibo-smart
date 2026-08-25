package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.streaming.StreamingRepository
import intelbras.mobi.smart.domain.streaming.model.StreamingSession
import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference
import kotlin.coroutines.cancellation.CancellationException

internal class StreamingUsageReading(
    private val streamingRepository: StreamingRepository,
) {

    suspend operator fun invoke(sessionId: String): StreamingUsageResult {
        if (sessionId.isBlank()) return StreamingUsageResult.Unavailable

        return try {
            streamingRepository.readSession(StreamingSessionReference(sessionId)).toResult()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            StreamingUsageResult.Unavailable
        }
    }

    private fun StreamingSession.toResult() = StreamingUsageResult.Measured(
        StreamingUsage(
            consumedBytes = bytesConsumed,
            remainingQuotaGb = quotaRemainingGb,
            isActive = isActive,
            quotaExceeded = quotaExceeded,
        ),
    )
}
