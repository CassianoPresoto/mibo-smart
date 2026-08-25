package intelbras.mobi.smart.ui.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.VideoPlayback
import intelbras.mobi.smart.business.usecase.VideoPlaybackFailure
import intelbras.mobi.smart.business.usecase.VideoPlaybackState
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveVideoViewModel(
    private val videoPlayback: VideoPlayback,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<LiveVideoUiState>(LiveVideoUiState.Connecting)
    val uiState: StateFlow<LiveVideoUiState> = mutableUiState.asStateFlow()

    private var watched: WatchedDevice? = null
    private var playback: Job? = null

    fun onScreenOpened(device: DeviceReference, player: VideoPlayer) {
        watched = WatchedDevice(device, player)
        startPlayback()
    }

    fun onRetry() = startPlayback()

    fun onScreenClosed() {
        playback?.cancel()
        playback = null
        watched = null
    }

    override fun onCleared() {
        playback?.cancel()
    }

    private fun startPlayback() {
        val watched = watched ?: return

        playback?.cancel()
        mutableUiState.value = LiveVideoUiState.Connecting
        playback = viewModelScope.launch {
            videoPlayback.play(watched.device, watched.player).collect { state ->
                mutableUiState.value = state.toUiState()
            }
        }
    }

    private fun VideoPlaybackState.toUiState(): LiveVideoUiState = when (this) {
        VideoPlaybackState.Connecting -> LiveVideoUiState.Connecting
        VideoPlaybackState.Buffering -> LiveVideoUiState.Buffering
        VideoPlaybackState.Playing -> LiveVideoUiState.Playing
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
