package intelbras.mobi.smart.ui.feature.video.capture

import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.ui.feature.video.megabytesOf
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val NO_DURATION = ""
private const val MILLISECONDS_IN_A_SECOND = 1_000L
private const val SECONDS_IN_A_MINUTE = 60L
private const val TWO_DIGITS = 2
private const val PAD_GLYPH = '0'

internal fun List<CameraCapture>.toUiModels(timeZone: TimeZone): List<CameraCaptureUiModel> =
    map { capture -> capture.toUiModel(timeZone) }

internal fun CameraCapture.toUiModel(timeZone: TimeZone): CameraCaptureUiModel = CameraCaptureUiModel(
    id = id,
    kind = kindUiModel(),
    fileName = fileName,
    previewFileName = previewFileName,
    momentLabel = momentLabelOf(capturedAtEpochMilliseconds, timeZone),
    durationLabel = durationLabelOf(),
    sizeLabel = sizeLabelOf(sizeBytes),
)

internal fun elapsedLabelOf(seconds: Int): String {
    val minutes = seconds / SECONDS_IN_A_MINUTE.toInt()
    val remainingSeconds = seconds % SECONDS_IN_A_MINUTE.toInt()
    return "${minutes.padded()}:${remainingSeconds.padded()}"
}

private fun CameraCapture.kindUiModel(): CameraCaptureKindUiModel = when (this) {
    is CameraCapture.Photo -> CameraCaptureKindUiModel.Photo
    is CameraCapture.Clip -> CameraCaptureKindUiModel.Clip
}

private fun CameraCapture.durationLabelOf(): String = when (this) {
    is CameraCapture.Photo -> NO_DURATION
    is CameraCapture.Clip -> elapsedLabelOf((durationMilliseconds / MILLISECONDS_IN_A_SECOND).toInt())
}

private fun sizeLabelOf(sizeBytes: Long): String = megabytesOf(sizeBytes) + " MB"

private fun momentLabelOf(epochMilliseconds: Long, timeZone: TimeZone): String {
    val moment: LocalDateTime = Instant.fromEpochMilliseconds(epochMilliseconds).toLocalDateTime(timeZone)
    return "${moment.day.padded()}/${(moment.month.ordinal + 1).padded()} · " +
        "${moment.hour.padded()}:${moment.minute.padded()}"
}

private fun Int.padded(): String = toString().padStart(TWO_DIGITS, PAD_GLYPH)
