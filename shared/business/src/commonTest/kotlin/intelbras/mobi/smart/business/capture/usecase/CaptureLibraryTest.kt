package intelbras.mobi.smart.business.capture.usecase

import intelbras.mobi.smart.business.InMemoryCameraCaptureStore
import intelbras.mobi.smart.business.InMemoryMediaFileStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class CaptureLibraryTest {

    private val camera = DeviceReference(serialNumber = "KAYK0109140D9", productId = "iM3-C")
    private val captureStore = InMemoryCameraCaptureStore()
    private val mediaFileStore = InMemoryMediaFileStore()

    private val readLibrary = CaptureLibraryReading(captureStore)
    private val removeCapture = CaptureRemoval(captureStore, mediaFileStore)

    private val photo = CameraCapture.Photo(
        id = "capture-1",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "foto.jpg",
        capturedAtEpochMilliseconds = 1_724_589_000_000L,
        sizeBytes = 100L,
    )

    private val clip = CameraCapture.Clip(
        id = "capture-2",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "take.mp4",
        previewFileName = "take-capa.jpg",
        capturedAtEpochMilliseconds = 1_724_589_100_000L,
        sizeBytes = 200L,
        durationMilliseconds = 9_000L,
    )

    @Test
    fun `follows what this device has saved for the camera`() = runTest {
        captureStore.save(photo)
        captureStore.save(clip)

        val captures = readLibrary(camera).first()

        assertEquals(listOf("capture-1", "capture-2"), captures.map { capture -> capture.id })
    }

    @Test
    fun `erases the take file its cover frame and the record`() = runTest {
        captureStore.save(clip)

        val result = removeCapture(clip)

        assertEquals(CaptureRemovalResult.Removed, result)
        assertEquals(listOf("take.mp4", "take-capa.jpg"), mediaFileStore.deleted)
        assertEquals(listOf("capture-2"), captureStore.removed)
        assertTrue(captureStore.saved.isEmpty())
    }

    @Test
    fun `erases a photo only once because it is its own cover frame`() = runTest {
        captureStore.save(photo)

        removeCapture(photo)

        assertEquals(listOf("foto.jpg"), mediaFileStore.deleted)
    }
}
