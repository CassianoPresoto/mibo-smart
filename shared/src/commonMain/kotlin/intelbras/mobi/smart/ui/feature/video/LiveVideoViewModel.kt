package intelbras.mobi.smart.ui.feature.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.capture.CameraCaptures
import intelbras.mobi.smart.business.streaming.StreamingMonitor
import intelbras.mobi.smart.business.video.VideoPlayback
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingSession
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingStartResult
import intelbras.mobi.smart.business.capture.usecase.ClipSaveResult
import intelbras.mobi.smart.business.device.usecase.LiveVideoSession
import intelbras.mobi.smart.business.capture.usecase.PhotoCaptureResult
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageResult
import intelbras.mobi.smart.business.video.usecase.VideoPlaybackFailure
import intelbras.mobi.smart.business.video.usecase.VideoPlaybackState
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureUiState
import intelbras.mobi.smart.ui.feature.video.capture.CaptureNotice
import intelbras.mobi.smart.ui.feature.video.capture.CaptureRecordingUiState
import intelbras.mobi.smart.ui.feature.video.capture.toUiModels
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone

internal const val USAGE_POLL_INTERVAL_MS = 5_000L
internal const val RECORDING_TICK_MS = 1_000L

private const val FIRST_SECOND = 1

class LiveVideoViewModel(
    private val videoPlayback: VideoPlayback,
    private val streamingMonitor: StreamingMonitor,
    private val cameraCaptures: CameraCaptures,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<LiveVideoUiState>(LiveVideoUiState.Connecting)
    val uiState: StateFlow<LiveVideoUiState> = mutableUiState.asStateFlow()

    private val mutableDetails = MutableStateFlow(LiveVideoDetails())
    val details: StateFlow<LiveVideoDetails> = mutableDetails.asStateFlow()

    private val mutableCaptures = MutableStateFlow(CameraCaptureUiState())
    val captures: StateFlow<CameraCaptureUiState> = mutableCaptures.asStateFlow()

    private var watched: WatchedDevice? = null
    private var playback: Job? = null
    private var usagePolling: Job? = null
    private var library: Job? = null
    private var recordingTick: Job? = null
    private var recordingSession: ClipRecordingSession? = null

    fun onScreenOpened(device: DeviceReference, player: VideoPlayer) {
        watched = WatchedDevice(device, player)
        mutableCaptures.update { state ->
            state.copy(
                canTakePhoto = player is LiveFrameCapture,
                canRecord = player is LiveFrameCapture && player is LiveClipRecorder,
            )
        }
        watchLibrary(device)
        startPlayback()
    }

    fun onRetry() = startPlayback()

    fun onScreenClosed() {
        stopRecordingIfNeeded()
        playback?.cancel()
        playback = null
        watched = null
        stopUsagePolling()
        library?.cancel()
        library = null
    }

    fun onPhotoRequested() {
        val watched = watched ?: return
        val frameCapture = watched.player as? LiveFrameCapture ?: return
        if (mutableCaptures.value.isTakingPhoto) return

        mutableCaptures.update { state -> state.copy(isTakingPhoto = true) }
        viewModelScope.launch {
            val result = cameraCaptures.takePhoto(watched.device, frameCapture)
            mutableCaptures.update { state ->
                state.copy(isTakingPhoto = false, notice = result.toNotice())
            }
        }
    }

    fun onRecordingToggled() {
        if (mutableCaptures.value.isRecording) {
            stopRecordingIfNeeded()
            return
        }
        startRecording()
    }

    fun onNoticeShown() {
        mutableCaptures.update { state -> state.copy(notice = null) }
    }

    suspend fun previewOf(fileName: String): ByteArray? = cameraCaptures.mediaBytesOf(fileName)

    override fun onCleared() {
        playback?.cancel()
        usagePolling?.cancel()
        recordingTick?.cancel()
    }

    private fun startRecording() {
        val watched = watched ?: return
        val frameCapture = watched.player as? LiveFrameCapture ?: return
        val recorder = watched.player as? LiveClipRecorder ?: return
        if (mutableCaptures.value.isBusy) return

        viewModelScope.launch {
            when (val start = cameraCaptures.startClip(watched.device, recorder, frameCapture)) {
                is ClipRecordingStartResult.Started -> beginTicking(start.session)
                ClipRecordingStartResult.Unsupported -> announce(CaptureNotice.RecordingUnsupported)
                is ClipRecordingStartResult.Failed -> announce(CaptureNotice.Failed)
            }
        }
    }

    private fun beginTicking(session: ClipRecordingSession) {
        recordingSession = session
        mutableCaptures.update { state ->
            state.copy(recording = CaptureRecordingUiState.Recording(elapsedSeconds = 0))
        }
        recordingTick = viewModelScope.launch {
            var elapsed = FIRST_SECOND
            while (isActive) {
                delay(RECORDING_TICK_MS)
                mutableCaptures.update { state ->
                    state.copy(recording = CaptureRecordingUiState.Recording(elapsed))
                }
                elapsed++
            }
        }
    }

    private fun stopRecordingIfNeeded() {
        val session = recordingSession ?: return
        val recorder = watched?.player as? LiveClipRecorder ?: return

        recordingSession = null
        recordingTick?.cancel()
        recordingTick = null
        mutableCaptures.update { state -> state.copy(recording = CaptureRecordingUiState.Saving) }

        viewModelScope.launch {
            val result = withContext(NonCancellable) { cameraCaptures.finishClip(recorder, session) }
            mutableCaptures.update { state ->
                state.copy(recording = CaptureRecordingUiState.Idle, notice = result.toNotice())
            }
        }
    }

    private fun announce(notice: CaptureNotice) {
        mutableCaptures.update { state -> state.copy(notice = notice) }
    }

    private fun watchLibrary(device: DeviceReference) {
        library?.cancel()
        library = viewModelScope.launch {
            cameraCaptures.capturesOf(device).collect { captures ->
                val items = captures.toUiModels(TimeZone.currentSystemDefault())
                mutableCaptures.update { state -> state.copy(captures = items) }
            }
        }
    }

    private fun startPlayback() {
        val watched = watched ?: return

        playback?.cancel()
        stopUsagePolling()
        mutableUiState.value = LiveVideoUiState.Connecting
        playback = viewModelScope.launch {
            videoPlayback.play(watched.device, watched.player).collect { state ->
                mutableUiState.value = state.toUiState()
                if (state is VideoPlaybackState.Playing) rememberSession(state.session)
                if (state.stopsTheCapture()) stopRecordingIfNeeded()
            }
        }
    }

    private fun VideoPlaybackState.stopsTheCapture(): Boolean =
        this is VideoPlaybackState.Ended || this is VideoPlaybackState.Failed

    private fun rememberSession(session: LiveVideoSession) {
        val isNewSession = mutableDetails.value.sessionId != session.sessionId
        mutableDetails.update { details ->
            details.copy(
                sessionId = session.sessionId,
                quotaGb = session.quotaGb,
                usage = if (isNewSession) null else details.usage,
            )
        }
        if (isNewSession) startUsagePolling(session.sessionId)
    }

    private fun startUsagePolling(sessionId: String) {
        stopUsagePolling()
        usagePolling = viewModelScope.launch {
            while (isActive) {
                fetchUsage(sessionId)
                delay(USAGE_POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopUsagePolling() {
        usagePolling?.cancel()
        usagePolling = null
    }

    private suspend fun fetchUsage(sessionId: String) {
        val isFirstReading = mutableDetails.value.usage == null
        if (isFirstReading) mutableDetails.update { details -> details.copy(isReadingUsage = true) }

        val measured = streamingMonitor.usageOf(sessionId).toUsage()
        mutableDetails.update { details ->
            details.copy(isReadingUsage = false, usage = measured ?: details.usage)
        }
    }

    private fun PhotoCaptureResult.toNotice(): CaptureNotice = when (this) {
        is PhotoCaptureResult.Saved -> CaptureNotice.PhotoSaved
        PhotoCaptureResult.Unavailable -> CaptureNotice.FrameUnavailable
        is PhotoCaptureResult.Failed -> CaptureNotice.Failed
    }

    private fun ClipSaveResult.toNotice(): CaptureNotice = when (this) {
        is ClipSaveResult.Saved -> CaptureNotice.ClipSaved
        ClipSaveResult.NothingRecorded -> CaptureNotice.NothingRecorded
        is ClipSaveResult.Failed -> CaptureNotice.Failed
    }

    private fun StreamingUsageResult.toUsage(): LiveVideoUsage? = when (this) {
        is StreamingUsageResult.Measured -> LiveVideoUsage(
            consumedBytes = usage.consumedBytes,
            remainingQuotaGb = usage.remainingQuotaGb,
            isSessionActive = usage.isActive,
        )

        StreamingUsageResult.Unavailable -> null
    }

    private fun VideoPlaybackState.toUiState(): LiveVideoUiState = when (this) {
        VideoPlaybackState.Connecting -> LiveVideoUiState.Connecting
        VideoPlaybackState.Buffering -> LiveVideoUiState.Buffering
        is VideoPlaybackState.Playing -> LiveVideoUiState.Playing
        is VideoPlaybackState.Reconnecting -> LiveVideoUiState.Reconnecting(attempt)
        VideoPlaybackState.Ended -> LiveVideoUiState.Ended
        is VideoPlaybackState.Failed -> LiveVideoUiState.Failed(failure.toUiFailure())
    }

    private fun VideoPlaybackFailure.toUiFailure(): LiveVideoFailure = when (this) {
        VideoPlaybackFailure.NotSupported -> LiveVideoFailure.NotSupported
        VideoPlaybackFailure.DeviceOffline -> LiveVideoFailure.DeviceOffline
        VideoPlaybackFailure.QuotaExceeded -> LiveVideoFailure.QuotaExceeded
        VideoPlaybackFailure.InvalidToken -> LiveVideoFailure.SessionExpired
        VideoPlaybackFailure.NetworkUnavailable -> LiveVideoFailure.NetworkUnavailable
        VideoPlaybackFailure.PlaybackInterrupted -> LiveVideoFailure.PlaybackInterrupted
        is VideoPlaybackFailure.Unexpected -> LiveVideoFailure.Unexpected
    }

    private data class WatchedDevice(
        val device: DeviceReference,
        val player: VideoPlayer,
    )
}
