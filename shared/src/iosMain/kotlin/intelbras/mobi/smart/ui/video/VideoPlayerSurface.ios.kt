package intelbras.mobi.smart.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Composable
actual fun rememberVideoPlayer(): VideoPlayer = remember { PendingIosVideoPlayer() }

@Composable
actual fun VideoPlayerSurface(player: VideoPlayer, modifier: Modifier) = Unit

private class PendingIosVideoPlayer : VideoPlayer {

    private val reported = MutableSharedFlow<VideoPlayerEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<VideoPlayerEvent> = reported.asSharedFlow()

    override fun start(source: PlaybackSource) {
        reported.tryEmit(VideoPlayerEvent.Failed(PlaybackFailure.Playback))
    }

    override fun stop() = Unit
}
