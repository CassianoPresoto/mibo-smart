package intelbras.mobi.smart.ui.feature.video

import android.content.Context
import android.view.TextureView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import intelbras.mobi.smart.player.LiveClipSink
import intelbras.mobi.smart.player.RecordingDataSourceFactory
import intelbras.mobi.smart.player.VideoFrameSnapshot
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val EVENT_BUFFER = 16
private const val LAST_EVENT = 1

internal class ExoPlayerVideoPlayer(context: Context) :
    VideoPlayer,
    LiveFrameCapture,
    LiveClipRecorder {

    private val reported = MutableSharedFlow<VideoPlayerEvent>(
        replay = LAST_EVENT,
        extraBufferCapacity = EVENT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val clipSink = LiveClipSink()
    private val frameSnapshot = VideoFrameSnapshot()
    private var videoSurface: TextureView? = null

    override val events: Flow<VideoPlayerEvent> = reported.asSharedFlow()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                RecordingDataSourceFactory(DefaultDataSource.Factory(context), clipSink),
            ),
        )
        .build()
        .apply {
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
        reported.resetReplayCache()
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

    fun attachSurface(surface: TextureView?) {
        videoSurface = surface
    }

    override suspend fun captureFrame(destination: MediaFileDestination): FrameCaptureResult =
        frameSnapshot.capture(videoSurface, destination)

    override suspend fun startRecording(destination: MediaFileDestination): ClipRecordingStart =
        clipSink.startRecording(destination)

    override suspend fun finishRecording(): ClipRecordingOutcome = clipSink.finishRecording()

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
