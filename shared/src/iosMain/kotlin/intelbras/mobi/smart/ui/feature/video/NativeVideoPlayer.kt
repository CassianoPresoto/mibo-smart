package intelbras.mobi.smart.ui.feature.video

import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.UIKit.UIView

private const val EVENT_BUFFER = 16
private const val LAST_EVENT = 1

internal class NativeVideoPlayer(
    private val playback: NativeVideoPlayback,
) : VideoPlayer, NativeVideoPlaybackListener {

    private val reported = MutableSharedFlow<VideoPlayerEvent>(
        replay = LAST_EVENT,
        extraBufferCapacity = EVENT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<VideoPlayerEvent> = reported.asSharedFlow()

    fun view(): UIView = playback.view()

    override fun start(source: PlaybackSource) {
        reported.resetReplayCache()
        reported.tryEmit(VideoPlayerEvent.Buffering)
        playback.start(source.url, this)
    }

    override fun stop() {
        playback.stop()
    }

    override fun onBuffering() {
        reported.tryEmit(VideoPlayerEvent.Buffering)
    }

    override fun onPlaying() {
        reported.tryEmit(VideoPlayerEvent.Playing)
    }

    override fun onEnded() {
        reported.tryEmit(VideoPlayerEvent.Ended)
    }

    override fun onFailed() {
        reported.tryEmit(VideoPlayerEvent.Failed(PlaybackFailure.StreamEnded))
    }
}
