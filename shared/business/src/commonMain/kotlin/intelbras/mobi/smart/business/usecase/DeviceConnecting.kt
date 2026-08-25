package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
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
import kotlin.coroutines.cancellation.CancellationException

internal class DeviceConnecting(
    private val deviceRepository: DeviceRepository,
    private val cameraRepository: CameraRepository,
) {

    suspend operator fun invoke(device: DeviceReference): DeviceConnectionResult = try {
        openConnection(device)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toResult()
    }

    private suspend fun openConnection(device: DeviceReference): DeviceConnectionResult {
        val capabilities = deviceRepository.readCapabilities(DeviceSerial(device.serialNumber))
        if (!capabilities.streamLiveVideo()) return DeviceConnectionResult.NotSupported

        return DeviceConnectionResult.Connected(openLiveVideo(device))
    }

    private suspend fun openLiveVideo(device: DeviceReference): DeviceConnection.LiveVideo {
        val stream = cameraRepository.openVideoStream(
            VideoStreamRequest(serialNumber = device.serialNumber),
        )
        return DeviceConnection.LiveVideo(stream.toSession())
    }

    private fun DeviceCapabilities.streamLiveVideo(): Boolean =
        values.any { it.startsWith(LIVE_VIDEO_CAPABILITY, ignoreCase = true) }

    private fun VideoStream.toSession() = LiveVideoSession(
        streamUrl = url,
        sessionId = sessionId,
        quotaGb = quotaGb,
    )

    private fun Throwable.toResult(): DeviceConnectionResult = when {
        rejectsTheAccessToken() -> DeviceConnectionResult.InvalidToken
        this is SmartHomeQuotaExceededException -> DeviceConnectionResult.QuotaExceeded
        this is SmartHomeDeviceOfflineException -> DeviceConnectionResult.DeviceOffline
        this is SmartHomeNetworkException -> DeviceConnectionResult.NetworkUnavailable
        else -> DeviceConnectionResult.Error(this)
    }

    private companion object {
        const val LIVE_VIDEO_CAPABILITY = "RTSV"
    }
}
