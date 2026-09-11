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
import intelbras.mobi.smart.business.capture.CameraCaptures
import intelbras.mobi.smart.business.streaming.StreamingMonitor
import intelbras.mobi.smart.business.video.VideoPlayback
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingSession
import intelbras.mobi.smart.business.capture.usecase.ClipRecordingStartResult
import intelbras.mobi.smart.business.capture.usecase.ClipSaveResult
import intelbras.mobi.smart.business.device.usecase.LiveVideoSession
import intelbras.mobi.smart.business.capture.usecase.PhotoCaptureResult
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsage
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageResult
import intelbras.mobi.smart.business.video.usecase.VideoPlaybackFailure
import intelbras.mobi.smart.business.video.usecase.VideoPlaybackState
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import intelbras.mobi.smart.ui.feature.video.capture.CaptureNotice
import intelbras.mobi.smart.ui.feature.video.capture.CaptureRecordingUiState
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
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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

    private val cameraCaptures = mock<CameraCaptures> {
        every { capturesOf(any()) } returns flowOf(emptyList())
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
        val viewModel = LiveVideoViewModel(playback, streamingMonitor, cameraCaptures)

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
        val viewModel = LiveVideoViewModel(playback, streamingMonitor, cameraCaptures)
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
        val viewModel = LiveVideoViewModel(playback, streamingMonitor, cameraCaptures)
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

    @Test
    fun `offers the capture controls when the player knows how to capture`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))

        viewModel.onScreenOpened(camera, capturingPlayer)
        testScheduler.runCurrent()

        assertTrue(viewModel.captures.value.canTakePhoto)
        assertTrue(viewModel.captures.value.canRecord)
        viewModel.onScreenClosed()
    }

    @Test
    fun `hides the capture controls when the player cannot capture`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))

        viewModel.onScreenOpened(camera, player)
        testScheduler.runCurrent()

        assertEquals(false, viewModel.captures.value.canTakePhoto)
        assertEquals(false, viewModel.captures.value.canRecord)
        viewModel.onScreenClosed()
    }

    @Test
    fun `announces the photo saved on this device`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        everySuspend { cameraCaptures.takePhoto(any(), any()) } returns PhotoCaptureResult.Saved(savedPhoto)
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, capturingPlayer)
        testScheduler.runCurrent()

        viewModel.onPhotoRequested()
        testScheduler.runCurrent()

        assertEquals(CaptureNotice.PhotoSaved, viewModel.captures.value.notice)
        assertEquals(false, viewModel.captures.value.isTakingPhoto)
        verifySuspend { cameraCaptures.takePhoto(camera, capturingPlayer) }
        viewModel.onScreenClosed()
    }

    @Test
    fun `warns when the picture is not ready to be captured`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        everySuspend { cameraCaptures.takePhoto(any(), any()) } returns PhotoCaptureResult.Unavailable
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, capturingPlayer)
        testScheduler.runCurrent()

        viewModel.onPhotoRequested()
        testScheduler.runCurrent()

        assertEquals(CaptureNotice.FrameUnavailable, viewModel.captures.value.notice)
        viewModel.onScreenClosed()
    }

    @Test
    fun `counts the seconds while the take is being recorded`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        everySuspend { cameraCaptures.startClip(any(), any(), any()) } returns
            ClipRecordingStartResult.Started(clipSession)
        everySuspend { cameraCaptures.finishClip(any(), any()) } returns ClipSaveResult.Saved(savedClip)
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, capturingPlayer)
        testScheduler.runCurrent()

        viewModel.onRecordingToggled()
        testScheduler.runCurrent()
        testScheduler.advanceTimeBy(RECORDING_TICK_MS * 2)
        testScheduler.runCurrent()

        assertEquals(CaptureRecordingUiState.Recording(2), viewModel.captures.value.recording)
        viewModel.onScreenClosed()
    }

    @Test
    fun `saves the take when the user stops the recording`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        everySuspend { cameraCaptures.startClip(any(), any(), any()) } returns
            ClipRecordingStartResult.Started(clipSession)
        everySuspend { cameraCaptures.finishClip(any(), any()) } returns ClipSaveResult.Saved(savedClip)
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, capturingPlayer)
        testScheduler.runCurrent()
        viewModel.onRecordingToggled()
        testScheduler.runCurrent()

        viewModel.onRecordingToggled()
        testScheduler.runCurrent()

        assertEquals(CaptureRecordingUiState.Idle, viewModel.captures.value.recording)
        assertEquals(CaptureNotice.ClipSaved, viewModel.captures.value.notice)
        verifySuspend { cameraCaptures.finishClip(capturingPlayer, clipSession) }
        viewModel.onScreenClosed()
    }

    @Test
    fun `warns when the stream does not allow recording a take`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        everySuspend { cameraCaptures.startClip(any(), any(), any()) } returns
            ClipRecordingStartResult.Unsupported
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Playing(session)))
        viewModel.onScreenOpened(camera, capturingPlayer)
        testScheduler.runCurrent()

        viewModel.onRecordingToggled()
        testScheduler.runCurrent()

        assertEquals(CaptureRecordingUiState.Idle, viewModel.captures.value.recording)
        assertEquals(CaptureNotice.RecordingUnsupported, viewModel.captures.value.notice)
        viewModel.onScreenClosed()
    }

    @Test
    fun `closes the take when the transmission stops`() = runTest(testDispatcher) {
        val capturingPlayer = CapturingPlayer()
        val states = MutableSharedFlow<VideoPlaybackState>(replay = 1)
        val playback = mock<VideoPlayback> {
            every { play(any(), any()) } returns states
        }
        everySuspend { cameraCaptures.startClip(any(), any(), any()) } returns
            ClipRecordingStartResult.Started(clipSession)
        everySuspend { cameraCaptures.finishClip(any(), any()) } returns ClipSaveResult.Saved(savedClip)
        val viewModel = LiveVideoViewModel(playback, streamingMonitor, cameraCaptures)
        viewModel.onScreenOpened(camera, capturingPlayer)
        states.emit(VideoPlaybackState.Playing(session))
        testScheduler.runCurrent()
        viewModel.onRecordingToggled()
        testScheduler.runCurrent()

        states.emit(VideoPlaybackState.Failed(VideoPlaybackFailure.PlaybackInterrupted))
        testScheduler.runCurrent()

        assertEquals(CaptureRecordingUiState.Idle, viewModel.captures.value.recording)
        verifySuspend { cameraCaptures.finishClip(capturingPlayer, clipSession) }
        viewModel.onScreenClosed()
    }

    private val savedPhoto = CameraCapture.Photo(
        id = "capture-1",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "foto.jpg",
        capturedAtEpochMilliseconds = 1_724_570_000_000L,
        sizeBytes = 820_000L,
    )

    private val savedClip = CameraCapture.Clip(
        id = "capture-2",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "take.mp4",
        previewFileName = "take-capa.jpg",
        capturedAtEpochMilliseconds = 1_724_570_000_000L,
        sizeBytes = 4_200_000L,
        durationMilliseconds = 12_000L,
    )

    private val clipSession = ClipRecordingSession(
        deviceSerialNumber = "KAYK0109140D9",
        startedAtEpochMilliseconds = 1_724_570_000_000L,
        previewFileName = "take-capa.jpg",
    )

    private class CapturingPlayer : VideoPlayer, LiveFrameCapture, LiveClipRecorder {
        override val events: Flow<VideoPlayerEvent> = emptyFlow()

        override fun start(source: PlaybackSource) = Unit

        override fun stop() = Unit

        override suspend fun captureFrame(destination: MediaFileDestination): FrameCaptureResult =
            FrameCaptureResult.Captured(fileName = destination.fileName, sizeBytes = 1L)

        override suspend fun startRecording(destination: MediaFileDestination): ClipRecordingStart =
            ClipRecordingStart.Started

        override suspend fun finishRecording(): ClipRecordingOutcome =
            ClipRecordingOutcome.Recorded(fileName = "take.mp4", sizeBytes = 1L)
    }

    private fun viewModelFailingWith(failure: VideoPlaybackFailure): LiveVideoViewModel {
        val viewModel = viewModelWith(playbackOf(VideoPlaybackState.Failed(failure)))
        viewModel.onScreenOpened(camera, player)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun viewModelWith(playback: VideoPlayback) =
        LiveVideoViewModel(playback, streamingMonitor, cameraCaptures)

    private fun playbackOf(vararg states: VideoPlaybackState) = mock<VideoPlayback> {
        every { play(any(), any()) } returns flowOf(*states)
    }
}
