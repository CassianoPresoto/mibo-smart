package intelbras.mobi.smart.persistence.capture

import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.persistence.testDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class StoredCameraCapturesTest {

    private val captures = StoredCameraCaptures(testDatabase(), Dispatchers.Default)

    private val photo = CameraCapture.Photo(
        id = "capture-1",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "foto.jpg",
        capturedAtEpochMilliseconds = 1_724_589_000_000L,
        sizeBytes = 838_860L,
    )

    private val clip = CameraCapture.Clip(
        id = "capture-2",
        deviceSerialNumber = "KAYK0109140D9",
        fileName = "take.mp4",
        previewFileName = "take-capa.jpg",
        capturedAtEpochMilliseconds = 1_724_589_100_000L,
        sizeBytes = 4_404_019L,
        durationMilliseconds = 12_000L,
    )

    @Test
    fun `keeps a photo the camera saved on this device`() = runTest {
        captures.save(photo)

        val saved = captures.capturesOf("KAYK0109140D9").first().single()

        assertEquals(photo, assertIs<CameraCapture.Photo>(saved))
    }

    @Test
    fun `keeps a take with how long it lasted`() = runTest {
        captures.save(clip)

        val saved = captures.capturesOf("KAYK0109140D9").first().single()

        assertEquals(clip, assertIs<CameraCapture.Clip>(saved))
    }

    @Test
    fun `shows the newest capture first`() = runTest {
        captures.save(photo)
        captures.save(clip)

        val saved = captures.capturesOf("KAYK0109140D9").first()

        assertEquals(listOf("capture-2", "capture-1"), saved.map { capture -> capture.id })
    }

    @Test
    fun `only answers for the camera that was asked`() = runTest {
        captures.save(photo)
        captures.save(clip.copy(id = "capture-3", deviceSerialNumber = "OUTRA-CAMERA"))

        val saved = captures.capturesOf("KAYK0109140D9").first()

        assertEquals(listOf("capture-1"), saved.map { capture -> capture.id })
    }

    @Test
    fun `forgets a capture that was removed`() = runTest {
        captures.save(photo)

        captures.remove("capture-1")

        assertTrue(captures.capturesOf("KAYK0109140D9").first().isEmpty())
    }
}
