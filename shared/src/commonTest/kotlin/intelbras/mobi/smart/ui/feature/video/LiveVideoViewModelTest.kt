package intelbras.mobi.smart.ui.feature.video

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.streaming.StreamingMonitor
import intelbras.mobi.smart.business.video.VideoPlayback
import intelbras.mobi.smart.business.device.usecase.LiveVideoSession
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsage
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageResult
import intelbras.mobi.smart.business.video.usecase.VideoPlaybackFailure
import intelbras.mobi.smart.business.video.usecase.VideoPlaybackState
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LiveVideoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val camera = DeviceReference(serialNumber = "KAYK0109140D9", productId = "iM3-C")
    private val player = mock<VideoPlayer>()

    private val session = LiveVideoSession(
        streamUrl = "https://open-casainteligente/stream/1",
        sessionId = "session-1",
        quotaGb = 1.0,
    )

    private val streamingMonitor = mock<StreamingMonitor> {
        everySuspend { usageOf(any()) } returns StreamingUsageResult.Unavailable
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `waits for the connection as soon as the screen opens`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Connecting))

        viewModel.onScreenOpened(camera, player)

        assertEquals(LiveVideoUiState.Connecting, viewModel.uiState.value)
    }

    @Test
    fun `follows the playback until the picture is on the screen`() = runTest(testDispatcher) {
        val playback = playbackOf(
            VideoPlaybackState.Connecting,
            VideoPlaybackState.Buffering,
            VideoPlaybackState.Playing(session),
        )
        val viewModel = LiveVideoViewModel(playback, streamingMonitor)

        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        assertEquals(LiveVideoUiState.Playing, viewModel.uiState.value)
        viewModel.onScreenClosed()
    }

    @Test
    fun `shows the attempt while the video reconnects`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Reconnecting(2)))

        viewModel.onScreenOpened(camera, player)
        testScheduler.advanceUntilIdle()

        assertEquals(LiveVideoUiState.Reconnecting(2), viewModel.uiState.value)
    }

    @Test
    fun `asks for a new token when the session was refused`() = runTest(testDispatcher) {
        val viewModel = viewModelFailingWith(VideoPlaybackFailure.InvalidToken)

        assertEquals(LiveVideoUiState.Failed(LiveVideoFailure.SessionExpired), viewModel.uiState.value)
    }

    @Test
    fun `reports the exhausted quota`() = runTest(testDispatcher) {
        val viewModel = viewModelFailingWith(VideoPlaybackFailure.QuotaExceeded)

        assertEquals(LiveVideoUiState.Failed(LiveVideoFailure.QuotaExceeded), viewModel.uiState.value)
    }

    @Test
    fun `reports a device that cannot be watched`() = runTest(testDispatcher) {
        val viewModel = viewModelFailingWith(VideoPlaybackFailure.NotSupported)

        assertEquals(LiveVideoUiState.Failed(LiveVideoFailure.NotSupported), viewModel.uiState.value)
    }

    @Test
    fun `hides the cause of an unexpected failure from the screen`() = runTest(testDispatcher) {
        val failure = VideoPlaybackFailure.Unexpected(IllegalStateException("boom"))

        val viewModel = viewModelFailingWith(failure)

        assertEquals(LiveVideoUiState.Failed(LiveVideoFailure.Unexpected), viewModel.uiState.value)
    }

    @Test
    fun `retrying starts the playback again`() = runTest(testDispatcher) {
        val playback = playbackOf(VideoPlaybackState.Playing(session))
        val viewModel = LiveVideoViewModel(playback, streamingMonitor)
        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        viewModel.onRetry()
        testScheduler.runCurrent()

        verify(exhaustiveOrder) {
            playback.play(camera, player)
            playback.play(camera, player)
        }
    }

    @Test
    fun `leaving the screen stops following the playback`() = runTest(testDispatcher) {
        val states = MutableSharedFlow<VideoPlaybackState>(replay = 1)
        val playback = mock<VideoPlayback> {
            every { play(any(), any()) } returns states
        }
        val viewModel = LiveVideoViewModel(playback, streamingMonitor)
        viewModel.onScreenOpened(camera, player)
        states.emit(VideoPlaybackState.Playing(session))
        testScheduler.runCurrent()

        viewModel.onScreenClosed()
        states.emit(VideoPlaybackState.Ended)
        testScheduler.runCurrent()

        assertEquals(LiveVideoUiState.Playing, viewModel.uiState.value)
    }

    @Test
    fun `remembers the session of the video that is playing`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))

        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        val details = viewModel.details.value
        assertEquals("session-1", details.sessionId)
        assertEquals(1.0, details.quotaGb)
        viewModel.onScreenClosed()
    }

    @Test
    fun `starts reading the session usage as soon as it starts playing`() = runTest(testDispatcher) {
        everySuspend { streamingMonitor.usageOf(any()) } returns StreamingUsageResult.Measured(
            StreamingUsage(
                consumedBytes = 5_242_880L,
                remainingQuotaGb = 0.8,
                isActive = true,
                quotaExceeded = false,
            ),
        )
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))

        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        val details = viewModel.details.value
        assertEquals(LiveVideoUsage(5_242_880L, 0.8, isSessionActive = true), details.usage)
        verifySuspend { streamingMonitor.usageOf("session-1") }
        viewModel.onScreenClosed()
    }

    @Test
    fun `the details survive a session the platform does not describe`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))

        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        assertEquals(null, viewModel.details.value.usage)
        assertEquals(false, viewModel.details.value.isReadingUsage)
        viewModel.onScreenClosed()
    }

    @Test
    fun `keeps refreshing the session usage while the screen stays open`() = runTest(testDispatcher) {
        everySuspend { streamingMonitor.usageOf(any()) } returns StreamingUsageResult.Measured(
            StreamingUsage(
                consumedBytes = 1_048_576L,
                remainingQuotaGb = 0.9,
                isActive = true,
                quotaExceeded = false,
            ),
        )
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        testScheduler.advanceTimeBy(USAGE_POLL_INTERVAL_MS * 3)
        testScheduler.runCurrent()

        verifySuspend(VerifyMode.atLeast(4)) { streamingMonitor.usageOf("session-1") }
        viewModel.onScreenClosed()
    }

    @Test
    fun `stops refreshing the usage after leaving the screen`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        viewModel.onScreenClosed()
        testScheduler.advanceTimeBy(USAGE_POLL_INTERVAL_MS * 3)
        testScheduler.runCurrent()

        verifySuspend(VerifyMode.exactly(1)) { streamingMonitor.usageOf("session-1") }
    }

    private fun viewModelFailingWith(failure: VideoPlaybackFailure): LiveVideoViewModel {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Failed(failure)))
        viewModel.onScreenOpened(camera, player)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun viewModelWith(playback: VideoPlayback) =
        LiveVideoViewModel(playback, streamingMonitor)

    private fun playbackOf(vararg states: VideoPlaybackState) = mock<VideoPlayback> {
        every { play(any(), any()) } returns flowOf(*states)
    }
}
