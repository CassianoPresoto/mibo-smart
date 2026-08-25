package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.DeviceConnector
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

private const val NO_ATTEMPT_YET = 0

internal class LiveVideoPlayback(
    private val deviceConnector: DeviceConnector,
    private val retryPolicy: PlaybackRetryPolicy,
) {

    operator fun invoke(
        device: DeviceReference,
        player: VideoPlayer,
    ): Flow<VideoPlaybackState> = flow {
        var connection: DeviceConnection.LiveVideo? = null
        try {
            emit(VideoPlaybackState.Connecting)
            connection = openLiveVideo(device) ?: return@flow

            var attempt = NO_ATTEMPT_YET
            while (true) {
                val session = connection?.session ?: return@flow
                player.start(PlaybackSource.LiveVideo(session.streamUrl))

                val failure = playUntilItStops(player)
                if (failure == null) {
                    emit(VideoPlaybackState.Ended)
                    return@flow
                }
                if (!deservesAnotherAttempt(failure, attempt)) {
                    emit(VideoPlaybackState.Failed(VideoPlaybackFailure.PlaybackInterrupted))
                    return@flow
                }

                attempt++
                emit(VideoPlaybackState.Reconnecting(attempt))
                delay(retryPolicy.waitBefore(attempt))

                if (retryPolicy.reopensConnectionOn(attempt)) {
                    connection.let { deviceConnector.disconnect(it) }
                    connection = openLiveVideo(device) ?: return@flow
                }
            }
        } finally {
            withContext(NonCancellable) {
                player.stop()
                connection?.let { deviceConnector.disconnect(it) }
            }
        }
    }

    private fun deservesAnotherAttempt(failure: PlaybackFailure, attempt: Int): Boolean =
        retryPolicy.retriesAfter(failure) && retryPolicy.hasAnotherAttemptAfter(attempt)

    private suspend fun FlowCollector<VideoPlaybackState>.openLiveVideo(
        device: DeviceReference,
    ): DeviceConnection.LiveVideo? {
        val result = deviceConnector.connect(device)
        val liveVideo = result.liveVideoOrNull()
        if (liveVideo == null) emit(VideoPlaybackState.Failed(result.toFailure()))
        return liveVideo
    }

    private suspend fun FlowCollector<VideoPlaybackState>.playUntilItStops(
        player: VideoPlayer,
    ): PlaybackFailure? {
        val lastEvent = player.events
            .onEach { event -> emitProgressOf(event) }
            .firstOrNull { event -> event.stopsThePlayback() }

        return (lastEvent as? VideoPlayerEvent.Failed)?.failure
    }

    private suspend fun FlowCollector<VideoPlaybackState>.emitProgressOf(event: VideoPlayerEvent) {
        when (event) {
            VideoPlayerEvent.Buffering -> emit(VideoPlaybackState.Buffering)
            VideoPlayerEvent.Playing -> emit(VideoPlaybackState.Playing)
            VideoPlayerEvent.Ended, is VideoPlayerEvent.Failed -> Unit
        }
    }

    private fun VideoPlayerEvent.stopsThePlayback(): Boolean =
        this is VideoPlayerEvent.Ended || this is VideoPlayerEvent.Failed

    private fun DeviceConnectionResult.liveVideoOrNull(): DeviceConnection.LiveVideo? =
        (this as? DeviceConnectionResult.Connected)?.connection as? DeviceConnection.LiveVideo

    private fun DeviceConnectionResult.toFailure(): VideoPlaybackFailure = when (this) {
        is DeviceConnectionResult.Connected -> VideoPlaybackFailure.NotSupported
        DeviceConnectionResult.NotSupported -> VideoPlaybackFailure.NotSupported
        DeviceConnectionResult.DeviceOffline -> VideoPlaybackFailure.DeviceOffline
        DeviceConnectionResult.QuotaExceeded -> VideoPlaybackFailure.QuotaExceeded
        DeviceConnectionResult.InvalidToken -> VideoPlaybackFailure.InvalidToken
        DeviceConnectionResult.NetworkUnavailable -> VideoPlaybackFailure.NetworkUnavailable
        is DeviceConnectionResult.Error -> VideoPlaybackFailure.Unexpected(cause)
    }
}
