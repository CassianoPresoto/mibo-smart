package intelbras.mobi.smart.business

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.usecase.ConnectionTermination
import intelbras.mobi.smart.business.usecase.DeviceConnecting
import intelbras.mobi.smart.business.usecase.DeviceKindResolution
import intelbras.mobi.smart.business.usecase.DeviceConnection
import intelbras.mobi.smart.business.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.usecase.DisconnectionResult
import intelbras.mobi.smart.business.usecase.LiveVideoSession
import intelbras.mobi.smart.domain.camera.CameraRepository
import intelbras.mobi.smart.domain.camera.model.VideoStream
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.streaming.StreamingRepository
import intelbras.mobi.smart.domain.streaming.model.StreamingSessionReference
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeviceConnectorTest {

    private val camera = DeviceReference(serialNumber = "KAYK0109140D9", productId = "iM3-C")

    private val deviceRepository = mock<DeviceRepository> {
        everySuspend { readCapabilities(any()) } returns DeviceCapabilities("RTSV1")
    }
    private val cameraRepository = mock<CameraRepository> {
        everySuspend { openVideoStream(any()) } returns VideoStream(
            url = "https://open-casainteligente.intelbras.com.br/stream/abc",
            sessionId = "session-abc",
        )
    }
    private val streamingRepository = mock<StreamingRepository> {
        everySuspend { endSession(any()) } returns Unit
    }

    private val connector: DeviceConnector = DeviceConnectorImpl(
        DeviceConnecting(DeviceKindResolution(deviceRepository, noLock()), cameraRepository),
        ConnectionTermination(streamingRepository),
    )

    @Test
    fun `connects to the device and gives back the open connection`() = runTest {
        val result = connector.connect(camera)

        val connected = assertIs<DeviceConnectionResult.Connected>(result)
        assertIs<DeviceConnection.LiveVideo>(connected.connection)
    }

    @Test
    fun `disconnecting releases what the connection was holding`() = runTest {
        val connection = DeviceConnection.LiveVideo(
            LiveVideoSession(streamUrl = "url", sessionId = "session-abc", quotaGb = 1.0),
        )

        val result = connector.disconnect(connection)

        assertEquals(DisconnectionResult.Released, result)
        verifySuspend { streamingRepository.endSession(StreamingSessionReference("session-abc")) }
    }
}
