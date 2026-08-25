package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.domain.camera.CameraRepository
import intelbras.mobi.smart.domain.camera.model.VideoStream
import intelbras.mobi.smart.domain.camera.model.VideoStreamRequest
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeQuotaExceededException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeviceConnectingTest {

    private val camera = DeviceReference(serialNumber = "KAYK0109140D9", productId = "iM3-C")
    private val stream = VideoStream(
        url = "https://open-casainteligente.intelbras.com.br/stream/abc",
        sessionId = "session-abc",
        quotaGb = 1.0,
    )

    @Test
    fun `opens the live video of a device that streams`() = runTest {
        val result = connecting(capabilities = "RTSV1,AudioTalk")(camera)

        val connected = assertIs<DeviceConnectionResult.Connected>(result)
        val liveVideo = assertIs<DeviceConnection.LiveVideo>(connected.connection)
        assertEquals(
            LiveVideoSession(
                streamUrl = "https://open-casainteligente.intelbras.com.br/stream/abc",
                sessionId = "session-abc",
                quotaGb = 1.0,
            ),
            liveVideo.session,
        )
    }

    @Test
    fun `asks the capabilities and the stream of the chosen device`() = runTest {
        val deviceRepository = deviceRepositoryReturning("RTSV1")
        val cameraRepository = cameraRepositoryReturning(stream)

        DeviceConnecting(deviceRepository, cameraRepository)(camera)

        verifySuspend { deviceRepository.readCapabilities(DeviceSerial("KAYK0109140D9")) }
        verifySuspend {
            cameraRepository.openVideoStream(VideoStreamRequest(serialNumber = "KAYK0109140D9"))
        }
    }

    @Test
    fun `accepts a newer version of the streaming capability`() = runTest {
        val result = connecting(capabilities = "rtsv2")(camera)

        assertIs<DeviceConnectionResult.Connected>(result)
    }

    @Test
    fun `refuses a device that does not stream video`() = runTest {
        val cameraRepository = mock<CameraRepository>()
        val deviceRepository = deviceRepositoryReturning("LocalRecord,AudioTalk")

        val result = DeviceConnecting(deviceRepository, cameraRepository)(camera)

        assertEquals(DeviceConnectionResult.NotSupported, result)
        verifySuspend(not) { cameraRepository.openVideoStream(any()) }
    }

    @Test
    fun `refuses a device that reports no capability at all`() = runTest {
        val result = connecting(capabilities = "")(camera)

        assertEquals(DeviceConnectionResult.NotSupported, result)
    }

    @Test
    fun `reports the exhausted streaming quota`() = runTest {
        val result = failingToStream(SmartHomeQuotaExceededException("cota"))

        assertEquals(DeviceConnectionResult.QuotaExceeded, result)
    }

    @Test
    fun `reports a camera that is offline`() = runTest {
        val result = failingToStream(SmartHomeDeviceOfflineException("offline"))

        assertEquals(DeviceConnectionResult.DeviceOffline, result)
    }

    @Test
    fun `asks for a new token when the platform refuses the current one`() = runTest {
        val result = failingToStream(SmartHomeUnauthorizedException("token"))

        assertEquals(DeviceConnectionResult.InvalidToken, result)
    }

    @Test
    fun `reports that the platform is unreachable`() = runTest {
        val result = failingToStream(SmartHomeNetworkException())

        assertEquals(DeviceConnectionResult.NetworkUnavailable, result)
    }

    @Test
    fun `keeps an unexpected failure for the caller to see`() = runTest {
        val failure = SmartHomeServerException("boom")

        val result = failingToStream(failure)

        assertEquals(DeviceConnectionResult.Error(failure), result)
    }

    @Test
    fun `a failure reading the capabilities also becomes a result`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { readCapabilities(any()) } throws SmartHomeNetworkException()
        }

        val result = DeviceConnecting(deviceRepository, mock<CameraRepository>())(camera)

        assertEquals(DeviceConnectionResult.NetworkUnavailable, result)
    }

    private suspend fun failingToStream(failure: Throwable): DeviceConnectionResult {
        val cameraRepository = mock<CameraRepository> {
            everySuspend { openVideoStream(any()) } throws failure
        }
        return DeviceConnecting(deviceRepositoryReturning("RTSV1"), cameraRepository)(camera)
    }

    private fun connecting(capabilities: String) =
        DeviceConnecting(deviceRepositoryReturning(capabilities), cameraRepositoryReturning(stream))

    private fun deviceRepositoryReturning(capabilities: String) = mock<DeviceRepository> {
        everySuspend { readCapabilities(any()) } returns DeviceCapabilities(capabilities)
    }

    private fun cameraRepositoryReturning(stream: VideoStream) = mock<CameraRepository> {
        everySuspend { openVideoStream(any()) } returns stream
    }
}
