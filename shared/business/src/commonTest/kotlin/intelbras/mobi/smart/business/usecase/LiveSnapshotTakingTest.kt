package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.CAPTURED_SIZE_BYTES
import intelbras.mobi.smart.business.FakeFrameCapture
import intelbras.mobi.smart.business.FixedClock
import intelbras.mobi.smart.business.InMemoryCameraCaptureStore
import intelbras.mobi.smart.business.InMemoryMediaFileStore
import intelbras.mobi.smart.business.MEDIA_DIRECTORY
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LiveSnapshotTakingTest {

    private val camera = DeviceReference(serialNumber = "KAYK-0109140D9", productId = "iM3-C")
    private val mediaFileStore = InMemoryMediaFileStore()
    private val captureStore = InMemoryCameraCaptureStore()
    private val clock = FixedClock()

    private val takeSnapshot = LiveSnapshotTaking(
        mediaFileStore = mediaFileStore,
        captureStore = captureStore,
        fileNaming = CaptureFileNaming(),
        clock = clock,
    )

    @Test
    fun `saves the photo captured from the live picture`() = runTest {
        val frameCapture = FakeFrameCapture()

        val result = takeSnapshot(camera, frameCapture)

        val saved = assertIs<PhotoCaptureResult.Saved>(result)
        assertEquals("foto-KAYK0109140D9-${NOW.toEpochMilliseconds()}.jpg", saved.capture.fileName)
        assertEquals(camera.serialNumber, saved.capture.deviceSerialNumber)
        assertEquals(NOW.toEpochMilliseconds(), saved.capture.capturedAtEpochMilliseconds)
        assertEquals(CAPTURED_SIZE_BYTES, saved.capture.sizeBytes)
    }

    @Test
    fun `writes the photo inside the media directory of this device`() = runTest {
        val frameCapture = FakeFrameCapture()

        takeSnapshot(camera, frameCapture)

        assertEquals(MEDIA_DIRECTORY, frameCapture.destinations.single().directoryPath)
    }

    @Test
    fun `remembers the photo so the library can show it`() = runTest {
        takeSnapshot(camera, FakeFrameCapture())

        val remembered = captureStore.saved.single()
        assertIs<CameraCapture.Photo>(remembered)
        assertEquals(remembered.fileName, remembered.previewFileName)
    }

    @Test
    fun `keeps the name the platform gave to the file it wrote`() = runTest {
        val frameCapture = object : LiveFrameCapture {
            override suspend fun captureFrame(destination: MediaFileDestination) =
                FrameCaptureResult.Captured(fileName = "foto.png", sizeBytes = 10L)
        }

        val result = takeSnapshot(camera, frameCapture)

        assertEquals("foto.png", assertIs<PhotoCaptureResult.Saved>(result).capture.fileName)
    }

    @Test
    fun `saves nothing when the picture is not ready`() = runTest {
        val frameCapture = FakeFrameCapture(FrameCaptureResult.Unavailable)

        val result = takeSnapshot(camera, frameCapture)

        assertEquals(PhotoCaptureResult.Unavailable, result)
        assertTrue(captureStore.saved.isEmpty())
    }

    @Test
    fun `reports the failure the player ran into`() = runTest {
        val cause = IllegalStateException("sem superfície")
        val frameCapture = FakeFrameCapture(FrameCaptureResult.Failed(cause))

        val result = takeSnapshot(camera, frameCapture)

        assertEquals(cause, assertIs<PhotoCaptureResult.Failed>(result).cause)
        assertTrue(captureStore.saved.isEmpty())
    }
}
