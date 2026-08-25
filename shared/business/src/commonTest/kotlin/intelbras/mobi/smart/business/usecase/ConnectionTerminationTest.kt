package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.streaming.StreamingRepository
import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionTerminationTest {

    @Test
    fun `releases the streaming session of a live video`() = runTest {
        val streamingRepository = mock<StreamingRepository> {
            everySuspend { endSession(any()) } returns Unit
        }

        val result = ConnectionTermination(streamingRepository)(liveVideo("session-abc"))

        assertEquals(DisconnectionResult.Released, result)
        verifySuspend { streamingRepository.endSession(StreamingSessionReference("session-abc")) }
    }

    @Test
    fun `has nothing to release when the platform gave no session`() = runTest {
        val streamingRepository = mock<StreamingRepository>()

        val result = ConnectionTermination(streamingRepository)(liveVideo(sessionId = ""))

        assertEquals(DisconnectionResult.Released, result)
        verifySuspend(not) { streamingRepository.endSession(any()) }
    }

    @Test
    fun `reports a session that could not be released`() = runTest {
        val failure = SmartHomeNetworkException()
        val streamingRepository = mock<StreamingRepository> {
            everySuspend { endSession(any()) } throws failure
        }

        val result = ConnectionTermination(streamingRepository)(liveVideo("session-abc"))

        assertEquals(DisconnectionResult.Failed(failure), result)
    }

    private fun liveVideo(sessionId: String) = DeviceConnection.LiveVideo(
        LiveVideoSession(
            streamUrl = "https://open-casainteligente.intelbras.com.br/stream/abc",
            sessionId = sessionId,
            quotaGb = 1.0,
        ),
    )
}
