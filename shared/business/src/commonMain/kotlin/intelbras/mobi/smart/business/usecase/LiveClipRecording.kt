package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.capture.CameraCaptureStore
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val NO_PREVIEW = ""

internal class LiveClipRecording(
    private val mediaFileStore: MediaFileStore,
    private val captureStore: CameraCaptureStore,
    private val fileNaming: CaptureFileNaming,
    private val clock: Clock,
) {

    suspend fun start(
        device: DeviceReference,
        recorder: LiveClipRecorder,
        frameCapture: LiveFrameCapture,
    ): ClipRecordingStartResult = try {
        beginRecording(device, recorder, frameCapture)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        ClipRecordingStartResult.Failed(failure)
    }

    suspend fun finish(
        recorder: LiveClipRecorder,
        session: ClipRecordingSession,
    ): ClipSaveResult = try {
        saveRecording(recorder, session)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        ClipSaveResult.Failed(failure)
    }

    private suspend fun beginRecording(
        device: DeviceReference,
        recorder: LiveClipRecorder,
        frameCapture: LiveFrameCapture,
    ): ClipRecordingStartResult {
        val startedAt = clock.now().toEpochMilliseconds()
        val previewFileName = capturePreview(device, frameCapture, startedAt)
        val destination = mediaFileStore.destinationFor(fileNaming.clipFileName(device, startedAt))

        val start = recorder.startRecording(destination)
        if (start != ClipRecordingStart.Started) discardPreview(previewFileName)

        return when (start) {
            ClipRecordingStart.Started -> ClipRecordingStartResult.Started(
                ClipRecordingSession(
                    deviceSerialNumber = device.serialNumber,
                    startedAtEpochMilliseconds = startedAt,
                    previewFileName = previewFileName,
                ),
            )

            ClipRecordingStart.Unsupported -> ClipRecordingStartResult.Unsupported
            is ClipRecordingStart.Failed -> ClipRecordingStartResult.Failed(start.cause)
        }
    }

    private suspend fun capturePreview(
        device: DeviceReference,
        frameCapture: LiveFrameCapture,
        startedAt: Long,
    ): String {
        val destination = mediaFileStore.destinationFor(fileNaming.previewFileName(device, startedAt))
        val frame = frameCapture.captureFrame(destination)
        return (frame as? FrameCaptureResult.Captured)?.fileName ?: NO_PREVIEW
    }

    private suspend fun discardPreview(previewFileName: String) {
        if (previewFileName != NO_PREVIEW) mediaFileStore.delete(previewFileName)
    }

    private suspend fun saveRecording(
        recorder: LiveClipRecorder,
        session: ClipRecordingSession,
    ): ClipSaveResult = when (val outcome = recorder.finishRecording()) {
        is ClipRecordingOutcome.Recorded -> ClipSaveResult.Saved(remember(session, outcome))

        ClipRecordingOutcome.NothingRecorded -> {
            discardPreview(session.previewFileName)
            ClipSaveResult.NothingRecorded
        }

        is ClipRecordingOutcome.Failed -> {
            discardPreview(session.previewFileName)
            ClipSaveResult.Failed(outcome.cause)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun remember(
        session: ClipRecordingSession,
        outcome: ClipRecordingOutcome.Recorded,
    ): CameraCapture.Clip {
        val clip = CameraCapture.Clip(
            id = Uuid.random().toString(),
            deviceSerialNumber = session.deviceSerialNumber,
            fileName = outcome.fileName,
            previewFileName = session.previewFileName,
            capturedAtEpochMilliseconds = session.startedAtEpochMilliseconds,
            sizeBytes = outcome.sizeBytes,
            durationMilliseconds = clock.now().toEpochMilliseconds() - session.startedAtEpochMilliseconds,
        )
        captureStore.save(clip)
        return clip
    }
}
