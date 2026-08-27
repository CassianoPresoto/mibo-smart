package intelbras.mobi.smart.business.capture

import intelbras.mobi.smart.business.capture.usecase.CaptureLibraryReading
import intelbras.mobi.smart.business.capture.usecase.CaptureMediaReading
import intelbras.mobi.smart.business.capture.usecase.CaptureRemoval
import intelbras.mobi.smart.business.capture.usecase.CaptureRemovalResult
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingSession
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingStartResult
import intelbras.mobi.smart.business.capture.usecase.ClipSaveResult
import intelbras.mobi.smart.business.capture.usecase.LiveClipRecording
import intelbras.mobi.smart.business.capture.usecase.LiveSnapshotTaking
import intelbras.mobi.smart.business.capture.usecase.PhotoCaptureResult
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlinx.coroutines.flow.Flow

internal class CameraCapturesImpl(
    private val liveSnapshotTaking: LiveSnapshotTaking,
    private val liveClipRecording: LiveClipRecording,
    private val captureLibraryReading: CaptureLibraryReading,
    private val captureRemoval: CaptureRemoval,
    private val captureMediaReading: CaptureMediaReading,
) : CameraCaptures {

    override suspend fun takePhoto(
        device: DeviceReference,
        frameCapture: LiveFrameCapture,
    ): PhotoCaptureResult = liveSnapshotTaking(device, frameCapture)

    override suspend fun startClip(
        device: DeviceReference,
        recorder: LiveClipRecorder,
        frameCapture: LiveFrameCapture,
    ): ClipRecordingStartResult = liveClipRecording.start(device, recorder, frameCapture)

    override suspend fun finishClip(
        recorder: LiveClipRecorder,
        session: ClipRecordingSession,
    ): ClipSaveResult = liveClipRecording.finish(recorder, session)

    override fun capturesOf(device: DeviceReference): Flow<List<CameraCapture>> =
        captureLibraryReading(device)

    override suspend fun remove(capture: CameraCapture): CaptureRemovalResult =
        captureRemoval(capture)

    override suspend fun mediaBytesOf(fileName: String): ByteArray? =
        captureMediaReading.bytesOf(fileName)

    override fun mediaPathOf(fileName: String): String = captureMediaReading.pathOf(fileName)
}
