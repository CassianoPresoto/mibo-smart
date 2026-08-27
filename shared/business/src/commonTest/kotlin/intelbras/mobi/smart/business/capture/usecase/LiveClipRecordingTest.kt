package intelbras.mobi.smart.business.capture.usecase

import intelbras.mobi.smart.business.CAPTURED_SIZE_BYTES
import intelbras.mobi.smart.business.FakeClipRecorder
import intelbras.mobi.smart.business.FakeFrameCapture
import intelbras.mobi.smart.business.FixedClock
import intelbras.mobi.smart.business.InMemoryCameraCaptureStore
import intelbras.mobi.smart.business.InMemoryMediaFileStore
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class LiveClipRecordingTest {

    private val camera = DeviceReference(serialNumber = "KAYK-0109140D9", productId = "iM3-C")
    private val mediaFileStore = InMemoryMediaFileStore()
    private val captureStore = InMemoryCameraCaptureStore()
    private val clock = FixedClock()

    private val recordClip = LiveClipRecording(
        mediaFileStore = mediaFileStore,
        captureStore = captureStore,
        fileNaming = CaptureFileNaming(),
        clock = clock,
    )

    @Test
    fun `opens the take with a cover frame taken from the live picture`() = runTest {
        val frameCapture = FakeFrameCapture()
        val recorder = FakeClipRecorder()

        val start = recordClip.start(camera, recorder, frameCapture)

        val session = assertIs<ClipRecordingStartResult.Started>(start).session
        assertEquals("take-capa-KAYK0109140D9-${NOW.toEpochMilliseconds()}.jpg", session.previewFileName)
        assertEquals("take-KAYK0109140D9-${NOW.toEpochMilliseconds()}.mp4", recorder.destinations.single().fileName)
    }

    @Test
    fun `records the take even when the cover frame could not be captured`() = runTest {
        val frameCapture = FakeFrameCapture(FrameCaptureResult.Unavailable)

        val start = recordClip.start(camera, FakeClipRecorder(), frameCapture)

        assertEquals("", assertIs<ClipRecordingStartResult.Started>(start).session.previewFileName)
    }

    @Test
    fun `saves the take with how long it lasted`() = runTest {
        val recorder = FakeClipRecorder()
        val session = startedSession(recorder)
        clock.advanceTo(NOW + 12.seconds)

        val result = recordClip.finish(recorder, session)

        val clip = assertIs<ClipSaveResult.Saved>(result).capture
        assertEquals(12_000L, clip.durationMilliseconds)
        assertEquals("take.mp4", clip.fileName)
        assertEquals(CAPTURED_SIZE_BYTES, clip.sizeBytes)
        assertEquals(NOW.toEpochMilliseconds(), clip.capturedAtEpochMilliseconds)
    }

    @Test
    fun `remembers the take so the library can show it`() = runTest {
        val recorder = FakeClipRecorder()
        val session = startedSession(recorder)

        recordClip.finish(recorder, session)

        val remembered = assertIs<CameraCapture.Clip>(captureStore.saved.single())
        assertEquals(session.previewFileName, remembered.previewFileName)
    }

    @Test
    fun `throws away the cover frame when the stream does not allow recording`() = runTest {
        val frameCapture = FakeFrameCapture()
        val recorder = FakeClipRecorder(start = ClipRecordingStart.Unsupported)

        val start = recordClip.start(camera, recorder, frameCapture)

        assertEquals(ClipRecordingStartResult.Unsupported, start)
        assertEquals(
            listOf("take-capa-KAYK0109140D9-${NOW.toEpochMilliseconds()}.jpg"),
            mediaFileStore.deleted,
        )
    }

    @Test
    fun `reports the failure the recorder ran into when it started`() = runTest {
        val cause = IllegalStateException("sem espaço")
        val recorder = FakeClipRecorder(start = ClipRecordingStart.Failed(cause))

        val start = recordClip.start(camera, recorder, FakeFrameCapture())

        assertEquals(cause, assertIs<ClipRecordingStartResult.Failed>(start).cause)
    }

    @Test
    fun `saves nothing when the recording produced no video`() = runTest {
        val recorder = FakeClipRecorder(outcome = ClipRecordingOutcome.NothingRecorded)
        val session = startedSession(recorder)

        val result = recordClip.finish(recorder, session)

        assertEquals(ClipSaveResult.NothingRecorded, result)
        assertTrue(captureStore.saved.isEmpty())
        assertEquals(listOf(session.previewFileName), mediaFileStore.deleted)
    }

    @Test
    fun `throws away the cover frame when the recording failed`() = runTest {
        val cause = IllegalStateException("gravação interrompida")
        val recorder = FakeClipRecorder(outcome = ClipRecordingOutcome.Failed(cause))
        val session = startedSession(recorder)

        val result = recordClip.finish(recorder, session)

        assertEquals(cause, assertIs<ClipSaveResult.Failed>(result).cause)
        assertEquals(listOf(session.previewFileName), mediaFileStore.deleted)
    }

    private suspend fun startedSession(recorder: FakeClipRecorder): ClipRecordingSession {
        val start = recordClip.start(camera, recorder, FakeFrameCapture())
        return assertIs<ClipRecordingStartResult.Started>(start).session
    }
}
