package intelbras.mobi.smart.domain.capture.model

sealed interface CameraCapture {
    val id: String
    val deviceSerialNumber: String
    val fileName: String
    val previewFileName: String
    val capturedAtEpochMilliseconds: Long
    val sizeBytes: Long

    data class Photo(
        override val id: String,
        override val deviceSerialNumber: String,
        override val fileName: String,
        override val capturedAtEpochMilliseconds: Long,
        override val sizeBytes: Long,
    ) : CameraCapture {
        override val previewFileName: String = fileName
    }

    data class Clip(
        override val id: String,
        override val deviceSerialNumber: String,
        override val fileName: String,
        override val previewFileName: String,
        override val capturedAtEpochMilliseconds: Long,
        override val sizeBytes: Long,
        val durationMilliseconds: Long,
    ) : CameraCapture
}
