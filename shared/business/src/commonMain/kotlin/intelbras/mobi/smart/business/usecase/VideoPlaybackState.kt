package intelbras.mobi.smart.business.usecase

sealed interface VideoPlaybackState {
    data object Connecting : VideoPlaybackState

    data object Buffering : VideoPlaybackState

    data object Playing : VideoPlaybackState

    data class Reconnecting(val attempt: Int) : VideoPlaybackState

    data object Ended : VideoPlaybackState

    data class Failed(val failure: VideoPlaybackFailure) : VideoPlaybackState
}

sealed interface VideoPlaybackFailure {
    data object NotSupported : VideoPlaybackFailure

    data object DeviceOffline : VideoPlaybackFailure

    data object QuotaExceeded : VideoPlaybackFailure

    data object InvalidToken : VideoPlaybackFailure

    data object NetworkUnavailable : VideoPlaybackFailure

    data object PlaybackInterrupted : VideoPlaybackFailure

    data class Unexpected(val cause: Throwable) : VideoPlaybackFailure
}
