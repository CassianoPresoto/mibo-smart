package intelbras.mobi.smart.persistence.capture

import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.persistence.db.CameraCaptureEntity

internal const val PHOTO_KIND = "PHOTO"
internal const val CLIP_KIND = "CLIP"

internal fun CameraCapture.kindCode(): String = when (this) {
    is CameraCapture.Photo -> PHOTO_KIND
    is CameraCapture.Clip -> CLIP_KIND
}

internal fun CameraCapture.durationOrNull(): Long? = (this as? CameraCapture.Clip)?.durationMilliseconds

internal fun CameraCaptureEntity.toCaptureOrNull(): CameraCapture? = when (kind) {
    PHOTO_KIND -> CameraCapture.Photo(
        id = id,
        deviceSerialNumber = deviceSerialNumber,
        fileName = fileName,
        capturedAtEpochMilliseconds = capturedAtEpochMilliseconds,
        sizeBytes = sizeBytes,
    )

    CLIP_KIND -> CameraCapture.Clip(
        id = id,
        deviceSerialNumber = deviceSerialNumber,
        fileName = fileName,
        previewFileName = previewFileName,
        capturedAtEpochMilliseconds = capturedAtEpochMilliseconds,
        sizeBytes = sizeBytes,
        durationMilliseconds = durationMilliseconds ?: NO_DURATION,
    )

    else -> null
}

private const val NO_DURATION = 0L
