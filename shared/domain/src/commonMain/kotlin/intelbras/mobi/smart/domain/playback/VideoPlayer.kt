package intelbras.mobi.smart.domain.playback

import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.flow.Flow

interface VideoPlayer {
    val events: Flow<VideoPlayerEvent>

    fun start(source: PlaybackSource)

    fun stop()
}
