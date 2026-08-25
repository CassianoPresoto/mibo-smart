package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.FakeDeviceConnector
import intelbras.mobi.smart.business.FakeVideoPlayer
import intelbras.mobi.smart.business.connectionTo
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LiveVideoPlaybackTest {

    private val camera = DeviceReference(serialNumber = "KAYK0109140D9", productId = "iM3-C")
    private val retryPolicy = PlaybackRetryPolicy(maxAttempts = 3, waitBetweenAttempts = 2.seconds)

    @Test
    fun `announces the connection before anything plays`() = runTest {
        val watching = watch(FakeDeviceConnector(connectionTo("https://stream/1", "session-1")))

        assertEquals(listOf<VideoPlaybackState>(VideoPlaybackState.Connecting), watching.states)
    }

    @Test
    fun `plays the stream that the connection gave`() = runTest {
        val watching = watch(FakeDeviceConnector(connectionTo("https://stream/1", "session-1")))

        watching.report(VideoPlayerEvent.Buffering)
        watching.report(VideoPlayerEvent.Playing)

        assertEquals(
            listOf<PlaybackSource>(PlaybackSource.LiveVideo("https://stream/1")),
            watching.player.started,
        )
        assertEquals(
            listOf<VideoPlaybackState>(
                VideoPlaybackState.Connecting,
                VideoPlaybackState.Buffering,
                VideoPlaybackState.Playing,
            ),
            watching.states,
        )
    }

    @Test
    fun `stops with the reason the platform gave for refusing the connection`() = runTest {
        val watching = watch(FakeDeviceConnector(DeviceConnectionResult.QuotaExceeded))

        assertEquals(
            listOf<VideoPlaybackState>(
                VideoPlaybackState.Connecting,
                VideoPlaybackState.Failed(VideoPlaybackFailure.QuotaExceeded),
            ),
            watching.states,
        )
        assertTrue(watching.player.started.isEmpty())
    }

    @Test
    fun `a device that does not stream video never reaches the player`() = runTest {
        val watching = watch(FakeDeviceConnector(DeviceConnectionResult.NotSupported))

        assertEquals(
            VideoPlaybackState.Failed(VideoPlaybackFailure.NotSupported),
            watching.states.last(),
        )
        assertTrue(watching.player.started.isEmpty())
    }

    @Test
    fun `the first attempt reloads the same stream`() = runTest {
        val connector = FakeDeviceConnector(connectionTo("https://stream/1", "session-1"))
        val watching = watch(connector)

        watching.report(VideoPlayerEvent.Playing)
        watching.report(VideoPlayerEvent.Failed(PlaybackFailure.Network))
        advanceTimeBy(3.seconds)
        runCurrent()

        assertEquals(
            listOf<PlaybackSource>(
                PlaybackSource.LiveVideo("https://stream/1"),
                PlaybackSource.LiveVideo("https://stream/1"),
            ),
            watching.player.started,
        )
        assertEquals(1, connector.connected.size)
        assertTrue(watching.states.contains(VideoPlaybackState.Reconnecting(1)))
    }

    @Test
    fun `the next attempt opens a new connection and drops the old session`() = runTest {
        val connector = FakeDeviceConnector(
            connectionTo("https://stream/1", "session-1"),
            connectionTo("https://stream/2", "session-2"),
        )
        val watching = watch(connector)

        watching.failTwiceWithNetwork()

        assertEquals(
            listOf<PlaybackSource>(
                PlaybackSource.LiveVideo("https://stream/1"),
                PlaybackSource.LiveVideo("https://stream/1"),
                PlaybackSource.LiveVideo("https://stream/2"),
            ),
            watching.player.started,
        )
        assertEquals(2, connector.connected.size)
        assertEquals(listOf("session-1"), connector.disconnected.map { it.sessionId() })
    }

    @Test
    fun `waits longer before each attempt`() = runTest {
        val watching = watch(FakeDeviceConnector(connectionTo("https://stream/1", "session-1")))

        watching.report(VideoPlayerEvent.Failed(PlaybackFailure.Network))
        advanceTimeBy(1.seconds)
        runCurrent()
        assertEquals(1, watching.player.started.size)

        advanceTimeBy(2.seconds)
        runCurrent()
        assertEquals(2, watching.player.started.size)
    }

    @Test
    fun `gives up after the last attempt`() = runTest {
        val watching = watch(FakeDeviceConnector(connectionTo("https://stream/1", "session-1")))

        repeat(retryPolicy.maxAttempts + 1) {
            watching.report(VideoPlayerEvent.Failed(PlaybackFailure.StreamEnded))
            advanceTimeBy(10.seconds)
            runCurrent()
        }

        assertEquals(
            VideoPlaybackState.Failed(VideoPlaybackFailure.PlaybackInterrupted),
            watching.states.last(),
        )
    }

    @Test
    fun `does not try again when the failure is in the playback itself`() = runTest {
        val watching = watch(FakeDeviceConnector(connectionTo("https://stream/1", "session-1")))

        watching.report(VideoPlayerEvent.Failed(PlaybackFailure.Playback))
        advanceTimeBy(10.seconds)
        runCurrent()

        assertEquals(1, watching.player.started.size)
        assertEquals(
            VideoPlaybackState.Failed(VideoPlaybackFailure.PlaybackInterrupted),
            watching.states.last(),
        )
    }

    @Test
    fun `finishes when the stream ends by itself`() = runTest {
        val watching = watch(FakeDeviceConnector(connectionTo("https://stream/1", "session-1")))

        watching.report(VideoPlayerEvent.Ended)

        assertEquals(VideoPlaybackState.Ended, watching.states.last())
    }

    @Test
    fun `leaving the playback stops the player and releases the session`() = runTest {
        val connector = FakeDeviceConnector(connectionTo("https://stream/1", "session-1"))
        val watching = watch(connector)
        watching.report(VideoPlayerEvent.Playing)

        watching.stopWatching()

        assertEquals(1, watching.player.stops)
        assertEquals(listOf("session-1"), connector.disconnected.map { it.sessionId() })
    }

    private fun DeviceConnection.sessionId(): String = when (this) {
        is DeviceConnection.LiveVideo -> session.sessionId
    }

    private fun TestScope.watch(connector: FakeDeviceConnector): Watching {
        val player = FakeVideoPlayer()
        val states = mutableListOf<VideoPlaybackState>()
        val playback = LiveVideoPlayback(connector, retryPolicy)
        val job = backgroundScope.launch { playback(camera, player).toList(states) }
        runCurrent()

        return Watching(this, player, states, job)
    }

    private class Watching(
        private val scope: TestScope,
        val player: FakeVideoPlayer,
        val states: MutableList<VideoPlaybackState>,
        private val job: kotlinx.coroutines.Job,
    ) {
        fun report(event: VideoPlayerEvent) {
            player.report(event)
            scope.testScheduler.runCurrent()
        }

        fun failTwiceWithNetwork() {
            report(VideoPlayerEvent.Failed(PlaybackFailure.Network))
            scope.testScheduler.advanceTimeBy(3.seconds.inWholeMilliseconds)
            scope.testScheduler.runCurrent()
            report(VideoPlayerEvent.Failed(PlaybackFailure.Network))
            scope.testScheduler.advanceTimeBy(6.seconds.inWholeMilliseconds)
            scope.testScheduler.runCurrent()
        }

        fun stopWatching() {
            job.cancel()
            scope.testScheduler.runCurrent()
        }
    }
}
