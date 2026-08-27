package intelbras.mobi.smart.domain.capture.model

data class MediaFileDestination(
    val directoryPath: String,
    val fileName: String,
) {
    val path: String = "$directoryPath/$fileName"
}
