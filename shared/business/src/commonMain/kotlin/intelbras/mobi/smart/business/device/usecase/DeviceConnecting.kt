package intelbras.mobi.smart.business.device.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.camera.CameraRepository
import intelbras.mobi.smart.domain.camera.model.VideoStream
import intelbras.mobi.smart.domain.camera.model.VideoStreamRequest
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeQuotaExceededException
import kotlin.coroutines.cancellation.CancellationException

internal class DeviceConnecting(
    private val deviceKindResolution: DeviceKindResolution,
    private val cameraRepository: CameraRepository,
) {

    suspend operator fun invoke(device: DeviceReference): DeviceConnectionResult = try {
        openConnection(device)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toResult()
    }

    private suspend fun openConnection(device: DeviceReference): DeviceConnectionResult =
        when (deviceKindResolution.announcedKind(DeviceSerial(device.serialNumber))) {
            DeviceKind.Camera -> DeviceConnectionResult.Connected(openLiveVideo(device))
            DeviceKind.Lock,
            DeviceKind.Hub,
            DeviceKind.Unknown,
            -> DeviceConnectionResult.NotSupported
        }

    private suspend fun openLiveVideo(device: DeviceReference): DeviceConnection.LiveVideo {
        val stream = cameraRepository.openVideoStream(
            VideoStreamRequest(serialNumber = device.serialNumber),
        )
        return DeviceConnection.LiveVideo(stream.toSession())
    }

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
}
