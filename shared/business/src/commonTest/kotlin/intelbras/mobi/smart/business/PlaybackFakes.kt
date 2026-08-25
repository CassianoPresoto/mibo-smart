package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.DeviceConnection
import intelbras.mobi.smart.business.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.usecase.DisconnectionResult
import intelbras.mobi.smart.business.usecase.LiveVideoSession
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal fun liveVideo(streamUrl: String, sessionId: String) = DeviceConnection.LiveVideo(
    LiveVideoSession(streamUrl = streamUrl, sessionId = sessionId, quotaGb = 1.0),
)

internal fun connectionTo(streamUrl: String, sessionId: String) =
    DeviceConnectionResult.Connected(liveVideo(streamUrl, sessionId))

internal class FakeDeviceConnector(vararg results: DeviceConnectionResult) : DeviceConnector {

    private val pendingResults = results.toMutableList()

    val connected = mutableListOf<DeviceReference>()
    val disconnected = mutableListOf<DeviceConnection>()

    override suspend fun connect(device: DeviceReference): DeviceConnectionResult {
        connected += device
        return if (pendingResults.size > 1) pendingResults.removeFirst() else pendingResults.first()
    }

    override suspend fun disconnect(connection: DeviceConnection): DisconnectionResult {
        disconnected += connection
        return DisconnectionResult.Released
    }
}

internal class FakeVideoPlayer : VideoPlayer {

    private val emitted = Channel<VideoPlayerEvent>(Channel.UNLIMITED)

    override val events: Flow<VideoPlayerEvent> = emitted.receiveAsFlow()

    val started = mutableListOf<PlaybackSource>()
    var stops = 0
        private set

    override fun start(source: PlaybackSource) {
        started += source
    }

    override fun stop() {
        stops++
    }

    fun report(event: VideoPlayerEvent) {
        emitted.trySend(event)
    }
}
