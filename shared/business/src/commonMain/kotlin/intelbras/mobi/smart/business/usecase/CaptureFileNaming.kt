package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference

private const val PHOTO_PREFIX = "foto"
private const val CLIP_PREFIX = "take"
private const val PREVIEW_PREFIX = "take-capa"
private const val PHOTO_EXTENSION = "jpg"
private const val CLIP_EXTENSION = "mp4"

internal class CaptureFileNaming {

    fun photoFileName(device: DeviceReference, capturedAtEpochMilliseconds: Long): String =
        name(PHOTO_PREFIX, device, capturedAtEpochMilliseconds, PHOTO_EXTENSION)

    fun clipFileName(device: DeviceReference, capturedAtEpochMilliseconds: Long): String =
        name(CLIP_PREFIX, device, capturedAtEpochMilliseconds, CLIP_EXTENSION)

    fun previewFileName(device: DeviceReference, capturedAtEpochMilliseconds: Long): String =
        name(PREVIEW_PREFIX, device, capturedAtEpochMilliseconds, PHOTO_EXTENSION)

    private fun name(
        prefix: String,
        device: DeviceReference,
        capturedAtEpochMilliseconds: Long,
        extension: String,
    ): String = "$prefix-${device.readableSerial()}-$capturedAtEpochMilliseconds.$extension"

    private fun DeviceReference.readableSerial(): String =
        serialNumber.filter { character -> character.isLetterOrDigit() }
}
