package intelbras.mobi.smart.ui.feature.video

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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withTimeoutOrNull
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.UIKit.UIView

private const val EVENT_BUFFER = 16
private const val LAST_EVENT = 1
private const val SNAPSHOT_TIMEOUT_MS = 5_000L
private const val RECORDING_START_TIMEOUT_MS = 5_000L
private const val RECORDING_STOP_TIMEOUT_MS = 10_000L
private const val NO_SIZE = 0L

internal class NativeVideoPlayer(
    private val playback: NativeVideoPlayback,
) : VideoPlayer, LiveFrameCapture, LiveClipRecorder, NativeVideoPlaybackListener {

    private val reported = MutableSharedFlow<VideoPlayerEvent>(
        replay = LAST_EVENT,
        extraBufferCapacity = EVENT_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var snapshotAwaiting: CompletableDeferred<String?>? = null
    private var recordingStartAwaiting: CompletableDeferred<Unit>? = null
    private var recordingStopAwaiting: CompletableDeferred<String?>? = null

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

    override suspend fun captureFrame(destination: MediaFileDestination): FrameCaptureResult {
        val awaiting = CompletableDeferred<String?>()
        snapshotAwaiting = awaiting
        playback.takeSnapshot(destination.path)

        val path = withTimeoutOrNull(SNAPSHOT_TIMEOUT_MS) { awaiting.await() }
        snapshotAwaiting = null
        if (path == null) return FrameCaptureResult.Unavailable

        return FrameCaptureResult.Captured(
            fileName = path.substringAfterLast('/'),
            sizeBytes = sizeOf(path),
        )
    }

    override suspend fun startRecording(destination: MediaFileDestination): ClipRecordingStart {
        val awaiting = CompletableDeferred<Unit>()
        recordingStartAwaiting = awaiting
        playback.startRecording(destination.directoryPath)

        val started = withTimeoutOrNull(RECORDING_START_TIMEOUT_MS) { awaiting.await() }
        recordingStartAwaiting = null
        return if (started == null) ClipRecordingStart.Unsupported else ClipRecordingStart.Started
    }

    override suspend fun finishRecording(): ClipRecordingOutcome {
        val awaiting = CompletableDeferred<String?>()
        recordingStopAwaiting = awaiting
        playback.stopRecording()

        val path = withTimeoutOrNull(RECORDING_STOP_TIMEOUT_MS) { awaiting.await() }
        recordingStopAwaiting = null
        if (path == null) return ClipRecordingOutcome.NothingRecorded

        return ClipRecordingOutcome.Recorded(
            fileName = path.substringAfterLast('/'),
            sizeBytes = sizeOf(path),
        )
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

    override fun onSnapshotTaken(path: String?) {
        snapshotAwaiting?.complete(path)
    }

    override fun onRecordingStarted() {
        recordingStartAwaiting?.complete(Unit)
    }

    override fun onRecordingStopped(path: String?) {
        recordingStopAwaiting?.complete(path)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun sizeOf(path: String): Long {
        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
        return (attributes?.get(NSFileSize) as? Number)?.toLong() ?: NO_SIZE
    }
}
