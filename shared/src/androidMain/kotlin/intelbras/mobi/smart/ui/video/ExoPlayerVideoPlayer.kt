package intelbras.mobi.smart.ui.video

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val EVENT_BUFFER = 16

internal class ExoPlayerVideoPlayer(context: Context) : VideoPlayer {

    private val reported = MutableSharedFlow<VideoPlayerEvent>(
        extraBufferCapacity = EVENT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<VideoPlayerEvent> = reported.asSharedFlow()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) = report(state)

                override fun onPlayerError(error: PlaybackException) {
                    reported.tryEmit(VideoPlayerEvent.Failed(playbackFailureOf(error.errorCode)))
                }
            },
        )
    }

    override fun start(source: PlaybackSource) {
        exoPlayer.setMediaItem(MediaItem.fromUri(source.url))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    override fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    fun release() {
        exoPlayer.release()
    }

    private fun report(state: Int) {
        when (state) {
            Player.STATE_BUFFERING -> reported.tryEmit(VideoPlayerEvent.Buffering)
            Player.STATE_READY -> reported.tryEmit(VideoPlayerEvent.Playing)
            Player.STATE_ENDED -> reported.tryEmit(VideoPlayerEvent.Ended)
            else -> Unit
        }
    }
}

internal fun playbackFailureOf(errorCode: Int): PlaybackFailure = when (errorCode) {
    PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW,
    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
    -> PlaybackFailure.StreamEnded

    PlaybackException.ERROR_CODE_TIMEOUT -> PlaybackFailure.Network

    in NETWORK_ERROR_CODES -> PlaybackFailure.Network

    else -> PlaybackFailure.Playback
}

private val NETWORK_ERROR_CODES =
    PlaybackException.ERROR_CODE_IO_UNSPECIFIED..PlaybackException.ERROR_CODE_IO_NO_PERMISSION
