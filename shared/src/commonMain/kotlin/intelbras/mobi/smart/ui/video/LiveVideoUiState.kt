package intelbras.mobi.smart.ui.video

sealed interface LiveVideoUiState {
    data object Connecting : LiveVideoUiState

    data object Buffering : LiveVideoUiState

    data object Playing : LiveVideoUiState

    data class Reconnecting(val attempt: Int) : LiveVideoUiState

    data object Ended : LiveVideoUiState

    data class Failed(val failure: LiveVideoFailure) : LiveVideoUiState
}

sealed interface LiveVideoFailure {
    data object NotSupported : LiveVideoFailure

    data object DeviceOffline : LiveVideoFailure

    data object QuotaExceeded : LiveVideoFailure

    data object SessionExpired : LiveVideoFailure

    data object NetworkUnavailable : LiveVideoFailure

    data object PlaybackInterrupted : LiveVideoFailure

    data object Unexpected : LiveVideoFailure
}
