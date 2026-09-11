package intelbras.mobi.smart.ui.feature.video.capture

enum class CameraCaptureKindUiModel { Photo, Clip }

data class CameraCaptureUiModel(
    val id: String,
    val kind: CameraCaptureKindUiModel,
    val fileName: String,
    val previewFileName: String,
    val momentLabel: String,
    val durationLabel: String,
    val sizeLabel: String,
)
