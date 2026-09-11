package intelbras.mobi.smart.domain.capture.model

sealed interface FrameCaptureResult {
    data class Captured(val fileName: String, val sizeBytes: Long) : FrameCaptureResult

    data object Unavailable : FrameCaptureResult

    data class Failed(val cause: Throwable) : FrameCaptureResult
}
