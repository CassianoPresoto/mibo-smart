package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.streaming.StreamingRepository
import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference
import kotlin.coroutines.cancellation.CancellationException

internal class ConnectionTermination(
    private val streamingRepository: StreamingRepository,
) {

    suspend operator fun invoke(connection: DeviceConnection): DisconnectionResult = try {
        connection.release()
        DisconnectionResult.Released
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        DisconnectionResult.Failed(failure)
    }

    private suspend fun DeviceConnection.release() = when (this) {
        is DeviceConnection.LiveVideo -> endStreamingSession(session)
    }

    private suspend fun endStreamingSession(session: LiveVideoSession) {
        if (session.sessionId.isBlank()) return

        streamingRepository.endSession(StreamingSessionReference(session.sessionId))
    }
}
