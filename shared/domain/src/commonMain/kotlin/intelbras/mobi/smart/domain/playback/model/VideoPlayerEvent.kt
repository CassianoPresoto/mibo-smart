package intelbras.mobi.smart.domain.playback.model

sealed interface VideoPlayerEvent {
    data object Buffering : VideoPlayerEvent

    data object Playing : VideoPlayerEvent

    data object Ended : VideoPlayerEvent

    data class Failed(val failure: PlaybackFailure) : VideoPlayerEvent
}
