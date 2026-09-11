package intelbras.mobi.smart.domain.capture.model

sealed interface ClipRecordingStart {
    data object Started : ClipRecordingStart

    data object Unsupported : ClipRecordingStart

    data class Failed(val cause: Throwable) : ClipRecordingStart
}

sealed interface ClipRecordingOutcome {
    data class Recorded(val fileName: String, val sizeBytes: Long) : ClipRecordingOutcome

    data object NothingRecorded : ClipRecordingOutcome

    data class Failed(val cause: Throwable) : ClipRecordingOutcome
}
