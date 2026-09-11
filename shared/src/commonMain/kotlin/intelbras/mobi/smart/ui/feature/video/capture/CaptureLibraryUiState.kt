package intelbras.mobi.smart.ui.feature.video.capture

data class CaptureLibraryUiState(
    val isLoading: Boolean = true,
    val captures: List<CameraCaptureUiModel> = emptyList(),
) {
    val isEmpty: Boolean = !isLoading && captures.isEmpty()
}
