package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.camera.model.RecordingRequest
import intelbras.mobi.smart.domain.camera.model.RecordingStorage
import intelbras.mobi.smart.domain.camera.model.StreamProfile
import intelbras.mobi.smart.domain.camera.model.VideoStreamRequest
import intelbras.mobi.smart.rest.SERIAL_NUMBER
import intelbras.mobi.smart.rest.SESSION_ID
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeInvalidRequestException
import intelbras.mobi.smart.rest.SmartHomeQuotaExceededException
import intelbras.mobi.smart.rest.SmartHomeRecordingNotFoundException
import intelbras.mobi.smart.rest.bodyText
import intelbras.mobi.smart.rest.respondJson
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CameraRestRepositoryTest {

    private val recordingRequest = RecordingRequest(
        serialNumber = SERIAL_NUMBER,
        storage = RecordingStorage.Cloud,
        startDate = "2026-05-22 08:00:00",
        endDate = "2026-05-22 08:30:00",
    )

    @Test
    fun `openVideoStream posts the stream settings and reads the flat envelope`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = CameraRestRepository(
            testApiCaller(captured) {
                respondJson(
                    """
                    {"status":"sucesso","data":{
                      "url":"https://open-casainteligente.intelbras.com.br/stream/$SESSION_ID",
                      "monitor_url":"https://open-casainteligente.intelbras.com.br/monitor_stream.html?session_id=$SESSION_ID",
                      "session_id":"$SESSION_ID","quota_gb":1.0,"warning":"URL única, não compartilhe"}}
                    """.trimIndent()
                )
            }
        )

        val stream = repository.openVideoStream(
            VideoStreamRequest(
                serialNumber = SERIAL_NUMBER,
                streamGb = 0.5,
                videoChannel = 1,
                streamProfile = StreamProfile.Secondary,
            )
        )

        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals("/cameras/criar-fluxo-video/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SERIAL_NUMBER","stream_gb":0.5,"canalVideo":1,"streamId":1}""",
            recorded.bodyText(),
        )
        assertEquals(SESSION_ID, stream.sessionId)
        assertEquals(1.0, stream.quotaGb)
        assertEquals("URL única, não compartilhe", stream.warning)
    }

    @Test
    fun `openVideoStream fails with quota exceeded when the platform charges 402`() = runTest {
        val repository = CameraRestRepository(
            testApiCaller {
                respondJson(
                    """{"msg":"Quota de streaming insuficiente"}""",
                    HttpStatusCode.PaymentRequired
                )
            }
        )

        assertFailsWith<SmartHomeQuotaExceededException> {
            repository.openVideoStream(VideoStreamRequest(serialNumber = SERIAL_NUMBER))
        }
    }

    @Test
    fun `loadRecording posts the period and omits the absent product id`() =
        runTest {
            val captured = mutableListOf<HttpRequestData>()
            val repository = CameraRestRepository(
                testApiCaller(captured) {
                    respondJson("""{"status":"sucesso","data":{"url":"https://open-casainteligente.intelbras.com.br/hls/abc.m3u8"}}""")
                }
            )

            val playback = repository.loadRecording(recordingRequest)

            val recorded = captured.single()
            assertEquals("/cameras/gravacao/v1", recorded.url.encodedPath)
            assertEquals(
                """{"ns":"$SERIAL_NUMBER","tipoArmazenamento":"nuvem","dataInicio":"2026-05-22 08:00:00","dataFim":"2026-05-22 08:30:00","canalVideo":0,"record_gb":1.0}""",
                recorded.bodyText(),
            )
            assertEquals("https://open-casainteligente.intelbras.com.br/hls/abc.m3u8", playback.url)
        }

    @Test
    fun `loadRecording tells an offline device apart from other bad requests`() = runTest {
        val repository = CameraRestRepository(
            testApiCaller {
                respondJson(
                    """{"msg":"Dispositivo offline"}""",
                    HttpStatusCode.BadRequest
                )
            }
        )

        assertFailsWith<SmartHomeDeviceOfflineException> { repository.loadRecording(recordingRequest) }
    }

    @Test
    fun `loadRecording tells a period without video apart from other bad requests`() = runTest {
        val repository = CameraRestRepository(
            testApiCaller {
                respondJson(
                    """{"msg":"Nenhum vídeo no período"}""",
                    HttpStatusCode.BadRequest
                )
            }
        )

        assertFailsWith<SmartHomeRecordingNotFoundException> {
            repository.loadRecording(
                recordingRequest
            )
        }
    }

    @Test
    fun `loadRecording keeps other bad requests generic`() = runTest {
        val repository = CameraRestRepository(
            testApiCaller {
                respondJson(
                    """{"msg":"Intervalo acima de 30 minutos"}""",
                    HttpStatusCode.BadRequest
                )
            }
        )

        assertFailsWith<SmartHomeInvalidRequestException> {
            repository.loadRecording(
                recordingRequest
            )
        }
    }
}
