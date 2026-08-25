package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference
import intelbras.mobi.smart.rest.SESSION_ID
import intelbras.mobi.smart.rest.bodyText
import intelbras.mobi.smart.rest.respondAcknowledgement
import intelbras.mobi.smart.rest.respondJson
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class StreamingRestRepositoryTest {

    private val reference = StreamingSessionReference(sessionId = SESSION_ID)

    @Test
    fun `readAvailableQuota posts an empty body and returns the quota`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = StreamingRestRepository(
            testApiCaller(captured) {
                respondJson("""{"status":"sucesso","data":{"quota_gb":10.0,"used_gb":2.5,"remaining_gb":7.5}}""")
            }
        )

        val quota = repository.readAvailableQuota()

        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals("/streaming/cota-disponivel/v1", recorded.url.encodedPath)
        assertEquals("{}", recorded.bodyText())
        assertEquals(7.5, quota.remainingGb)
    }

    @Test
    fun `listSessions posts an empty body and returns the open sessions`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = StreamingRestRepository(
            testApiCaller(captured) {
                respondJson("""{"status":"sucesso","data":[{"session_id":"$SESSION_ID","is_active":true}]}""")
            }
        )

        val sessions = repository.listSessions()

        val recorded = captured.single()
        assertEquals("/streaming/minhas-sessoes/v1", recorded.url.encodedPath)
        assertEquals("{}", recorded.bodyText())
        assertEquals(SESSION_ID, sessions.single().sessionId)
        assertTrue(sessions.single().isActive)
    }

    @Test
    fun `readSession posts the session id and returns the consumption`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = StreamingRestRepository(
            testApiCaller(captured) {
                respondJson(
                    """
                    {"status":"sucesso","data":{"session_id":"$SESSION_ID","bytes_consumed":1048576,
                     "quota_remaining_gb":0.9,"is_active":true,"quota_exceeded":false}}
                    """.trimIndent()
                )
            }
        )

        val session = repository.readSession(reference)

        val recorded = captured.single()
        assertEquals("/streaming/sessao-info/v1", recorded.url.encodedPath)
        assertEquals("""{"session_id":"$SESSION_ID"}""", recorded.bodyText())
        assertEquals(1_048_576L, session.bytesConsumed)
        assertEquals(0.9, session.quotaRemainingGb)
    }

    @Test
    fun `endSession posts the session id`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = StreamingRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.endSession(reference)

        val recorded = captured.single()
        assertEquals("/streaming/encerrar-sessao/v1", recorded.url.encodedPath)
        assertEquals("""{"session_id":"$SESSION_ID"}""", recorded.bodyText())
    }
}
