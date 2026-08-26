package intelbras.mobi.smart.ui.feature.video.capture

import intelbras.mobi.smart.domain.capture.model.CameraCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.TimeZone

class CameraCaptureItemMapperTest {

    private val timeZone = TimeZone.of("America/Sao_Paulo")

    @Test
    fun `describes a photo with the moment it was taken`() {
        val photo = CameraCapture.Photo(
            id = "capture-1",
            deviceSerialNumber = "KAYK0109140D9",
            fileName = "foto.jpg",
            capturedAtEpochMilliseconds = 1_724_589_000_000L,
            sizeBytes = 838_860L,
        )

        val item = photo.toUiModel(timeZone)

        assertEquals(CameraCaptureKindUiModel.Photo, item.kind)
        assertEquals("foto.jpg", item.previewFileName)
        assertEquals("", item.durationLabel)
        assertEquals("0,8 MB", item.sizeLabel)
    }

    @Test
    fun `describes a take with how long it lasted`() {
        val clip = CameraCapture.Clip(
            id = "capture-2",
            deviceSerialNumber = "KAYK0109140D9",
            fileName = "take.mp4",
            previewFileName = "take-capa.jpg",
            capturedAtEpochMilliseconds = 1_724_589_000_000L,
            sizeBytes = 4_404_019L,
            durationMilliseconds = 72_000L,
        )

        val item = clip.toUiModel(timeZone)

        assertEquals(CameraCaptureKindUiModel.Clip, item.kind)
        assertEquals("take-capa.jpg", item.previewFileName)
        assertEquals("01:12", item.durationLabel)
        assertEquals("4,2 MB", item.sizeLabel)
    }

    @Test
    fun `writes the moment in the time zone of this device`() {
        val photo = CameraCapture.Photo(
            id = "capture-1",
            deviceSerialNumber = "KAYK0109140D9",
            fileName = "foto.jpg",
            capturedAtEpochMilliseconds = 1_724_589_000_000L,
            sizeBytes = 1L,
        )

        val saoPaulo = photo.toUiModel(TimeZone.of("America/Sao_Paulo")).momentLabel
        val utc = photo.toUiModel(TimeZone.UTC).momentLabel

        assertEquals("25/08 · 09:30", saoPaulo)
        assertEquals("25/08 · 12:30", utc)
    }

    @Test
    fun `keeps the captures in the order the store sent them`() {
        val photo = CameraCapture.Photo(
            id = "capture-1",
            deviceSerialNumber = "KAYK0109140D9",
            fileName = "foto.jpg",
            capturedAtEpochMilliseconds = 1_724_589_000_000L,
            sizeBytes = 1L,
        )
        val older = photo.copy(id = "capture-0", capturedAtEpochMilliseconds = 1_724_500_000_000L)

        val items = listOf(photo, older).toUiModels(timeZone)

        assertEquals(listOf("capture-1", "capture-0"), items.map { item -> item.id })
    }
}
