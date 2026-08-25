package intelbras.mobi.smart.ui.video

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exhaustiveOrder
import intelbras.mobi.smart.business.VideoPlayback
import intelbras.mobi.smart.business.usecase.VideoPlaybackFailure
import intelbras.mobi.smart.business.usecase.VideoPlaybackState
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

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `waits for the connection as soon as the screen opens`() = runTest(testDispatcher) {
        val viewModel = LiveVideoViewModel(playbackOf(VideoPlaybackState.Connecting))

        viewModel.onScreenOpened(camera, player)

        assertEquals(LiveVideoUiState.Connecting, viewModel.uiState.value)
    }

    @Test
    fun `follows the playback until the picture is on the screen`() = runTest(testDispatcher) {
        val playback = playbackOf(
            VideoPlaybackState.Connecting,
            VideoPlaybackState.Buffering,
            VideoPlaybackState.Playing,
        )
        val viewModel = LiveVideoViewModel(playback)

        viewModel.onScreenOpened(camera, player)
        testScheduler.advanceUntilIdle()

        assertEquals(LiveVideoUiState.Playing, viewModel.uiState.value)
    }

    @Test
    fun `shows the attempt while the video reconnects`() = runTest(testDispatcher) {
        val viewModel = LiveVideoViewModel(playbackOf(VideoPlaybackState.Reconnecting(2)))

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
        val playback = playbackOf(VideoPlaybackState.Playing)
        val viewModel = LiveVideoViewModel(playback)
        viewModel.onScreenOpened(camera, player)
        testScheduler.advanceUntilIdle()

        viewModel.onRetry()
        testScheduler.advanceUntilIdle()

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
        val viewModel = LiveVideoViewModel(playback)
        viewModel.onScreenOpened(camera, player)
        states.emit(VideoPlaybackState.Playing)
        testScheduler.advanceUntilIdle()

        viewModel.onScreenClosed()
        states.emit(VideoPlaybackState.Ended)
        testScheduler.advanceUntilIdle()

        assertEquals(LiveVideoUiState.Playing, viewModel.uiState.value)
    }

    private fun viewModelFailingWith(failure: VideoPlaybackFailure): LiveVideoViewModel {
        val viewModel = LiveVideoViewModel(playbackOf(VideoPlaybackState.Failed(failure)))
        viewModel.onScreenOpened(camera, player)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun playbackOf(vararg states: VideoPlaybackState) = mock<VideoPlayback> {
        every { play(any(), any()) } returns flowOf(*states)
    }
}
