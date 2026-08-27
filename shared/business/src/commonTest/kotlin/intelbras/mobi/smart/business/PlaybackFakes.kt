package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.device.DeviceConnector
import intelbras.mobi.smart.business.device.usecase.DeviceConnection
import intelbras.mobi.smart.business.device.usecase.DeviceConnectionResult
import intelbras.mobi.smart.business.device.usecase.DisconnectionResult
import intelbras.mobi.smart.business.device.usecase.LiveVideoSession
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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

internal class FakeVideoPlayer(
    private val reportedOnStart: List<VideoPlayerEvent> = emptyList(),
) : VideoPlayer {

    private val emitted = MutableSharedFlow<VideoPlayerEvent>(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<VideoPlayerEvent> = emitted.asSharedFlow()

    val started = mutableListOf<PlaybackSource>()
    var stops = 0
        private set

    override fun start(source: PlaybackSource) {
        emitted.resetReplayCache()
        started += source
        reportedOnStart.forEach { report(it) }
    }

    override fun stop() {
        stops++
    }

    fun report(event: VideoPlayerEvent) {
        emitted.tryEmit(event)
    }
}
