package intelbras.mobi.smart.ui.feature.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.StreamingMonitor
import intelbras.mobi.smart.business.VideoPlayback
import intelbras.mobi.smart.business.usecase.LiveVideoSession
import intelbras.mobi.smart.business.usecase.StreamingUsageResult
import intelbras.mobi.smart.business.usecase.VideoPlaybackFailure
import intelbras.mobi.smart.business.usecase.VideoPlaybackState
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal const val USAGE_POLL_INTERVAL_MS = 5_000L

class LiveVideoViewModel(
    private val videoPlayback: VideoPlayback,
    private val streamingMonitor: StreamingMonitor,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<LiveVideoUiState>(LiveVideoUiState.Connecting)
    val uiState: StateFlow<LiveVideoUiState> = mutableUiState.asStateFlow()

    private val mutableDetails = MutableStateFlow(LiveVideoDetails())
    val details: StateFlow<LiveVideoDetails> = mutableDetails.asStateFlow()

    private var watched: WatchedDevice? = null
    private var playback: Job? = null
    private var usagePolling: Job? = null

    fun onScreenOpened(device: DeviceReference, player: VideoPlayer) {
        watched = WatchedDevice(device, player)
        startPlayback()
    }

    fun onRetry() = startPlayback()

    fun onScreenClosed() {
        playback?.cancel()
        playback = null
        watched = null
        stopUsagePolling()
    }

    override fun onCleared() {
        playback?.cancel()
        usagePolling?.cancel()
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
            }
        }
    }

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
