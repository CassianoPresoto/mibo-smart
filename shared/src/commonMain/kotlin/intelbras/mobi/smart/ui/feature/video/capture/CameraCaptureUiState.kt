package intelbras.mobi.smart.ui.feature.video.capture

sealed interface CaptureRecordingUiState {
    data object Idle : CaptureRecordingUiState

    data class Recording(val elapsedSeconds: Int) : CaptureRecordingUiState

    data object Saving : CaptureRecordingUiState
}

sealed interface CaptureNotice {
    data object PhotoSaved : CaptureNotice

    data object ClipSaved : CaptureNotice

    data object FrameUnavailable : CaptureNotice

    data object RecordingUnsupported : CaptureNotice

    data object NothingRecorded : CaptureNotice

    data object Failed : CaptureNotice
}

data class CameraCaptureUiState(
    val canTakePhoto: Boolean = false,
    val canRecord: Boolean = false,
    val isTakingPhoto: Boolean = false,
    val recording: CaptureRecordingUiState = CaptureRecordingUiState.Idle,
    val notice: CaptureNotice? = null,
    val captures: List<CameraCaptureUiModel> = emptyList(),
) {
    val isRecording: Boolean = recording is CaptureRecordingUiState.Recording

    val isBusy: Boolean = isTakingPhoto || recording is CaptureRecordingUiState.Saving

    val hasCaptures: Boolean = captures.isNotEmpty()
}
