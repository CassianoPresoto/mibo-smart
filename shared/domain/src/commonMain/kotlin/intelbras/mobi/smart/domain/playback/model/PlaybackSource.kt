package intelbras.mobi.smart.domain.playback.model

sealed interface PlaybackSource {
    val url: String

    data class LiveVideo(override val url: String) : PlaybackSource

    data class RecordedClip(override val url: String) : PlaybackSource
}
