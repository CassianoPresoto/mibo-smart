package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.capture.model.CameraCapture

data class ClipRecordingSession(
    val deviceSerialNumber: String,
    val startedAtEpochMilliseconds: Long,
    val previewFileName: String,
)

sealed interface PhotoCaptureResult {
    data class Saved(val capture: CameraCapture.Photo) : PhotoCaptureResult

    data object Unavailable : PhotoCaptureResult

    data class Failed(val cause: Throwable) : PhotoCaptureResult
}

sealed interface ClipRecordingStartResult {
    data class Started(val session: ClipRecordingSession) : ClipRecordingStartResult

    data object Unsupported : ClipRecordingStartResult

    data class Failed(val cause: Throwable) : ClipRecordingStartResult
}

sealed interface ClipSaveResult {
    data class Saved(val capture: CameraCapture.Clip) : ClipSaveResult

    data object NothingRecorded : ClipSaveResult

    data class Failed(val cause: Throwable) : ClipSaveResult
}

sealed interface CaptureRemovalResult {
    data object Removed : CaptureRemovalResult

    data class Failed(val cause: Throwable) : CaptureRemovalResult
}
