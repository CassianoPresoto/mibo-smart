package intelbras.mobi.smart.business.capture.usecase

import intelbras.mobi.smart.domain.capture.CameraCaptureStore
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class LiveSnapshotTaking(
    private val mediaFileStore: MediaFileStore,
    private val captureStore: CameraCaptureStore,
    private val fileNaming: CaptureFileNaming,
    private val clock: Clock,
) {

    suspend operator fun invoke(
        device: DeviceReference,
        frameCapture: LiveFrameCapture,
    ): PhotoCaptureResult = try {
        savePhotoOf(device, frameCapture)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        PhotoCaptureResult.Failed(failure)
    }

    private suspend fun savePhotoOf(
        device: DeviceReference,
        frameCapture: LiveFrameCapture,
    ): PhotoCaptureResult {
        val takenAt = clock.now().toEpochMilliseconds()
        val destination = mediaFileStore.destinationFor(fileNaming.photoFileName(device, takenAt))

        return when (val frame = frameCapture.captureFrame(destination)) {
            is FrameCaptureResult.Captured -> PhotoCaptureResult.Saved(
                remember(device, takenAt, frame),
            )

            FrameCaptureResult.Unavailable -> PhotoCaptureResult.Unavailable
            is FrameCaptureResult.Failed -> PhotoCaptureResult.Failed(frame.cause)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun remember(
        device: DeviceReference,
        takenAt: Long,
        frame: FrameCaptureResult.Captured,
    ): CameraCapture.Photo {
        val photo = CameraCapture.Photo(
            id = Uuid.random().toString(),
            deviceSerialNumber = device.serialNumber,
            fileName = frame.fileName,
            capturedAtEpochMilliseconds = takenAt,
            sizeBytes = frame.sizeBytes,
        )
        captureStore.save(photo)
        return photo
    }
}
