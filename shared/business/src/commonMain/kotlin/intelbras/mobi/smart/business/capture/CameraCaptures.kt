package intelbras.mobi.smart.business.capture

import intelbras.mobi.smart.business.capture.usecase.CaptureRemovalResult
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingSession
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingStartResult
import intelbras.mobi.smart.business.capture.usecase.ClipSaveResult
import intelbras.mobi.smart.business.capture.usecase.PhotoCaptureResult
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlinx.coroutines.flow.Flow

interface CameraCaptures {
    suspend fun takePhoto(
        device: DeviceReference,
        frameCapture: LiveFrameCapture,
    ): PhotoCaptureResult

    suspend fun startClip(
        device: DeviceReference,
        recorder: LiveClipRecorder,
        frameCapture: LiveFrameCapture,
    ): ClipRecordingStartResult

    suspend fun finishClip(
        recorder: LiveClipRecorder,
        session: ClipRecordingSession,
    ): ClipSaveResult

    fun capturesOf(device: DeviceReference): Flow<List<CameraCapture>>

    suspend fun remove(capture: CameraCapture): CaptureRemovalResult

    suspend fun mediaBytesOf(fileName: String): ByteArray?

    fun mediaPathOf(fileName: String): String
}
