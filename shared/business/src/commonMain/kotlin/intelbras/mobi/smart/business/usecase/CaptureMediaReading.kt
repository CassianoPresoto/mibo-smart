package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.capture.MediaFileStore

internal class CaptureMediaReading(
    private val mediaFileStore: MediaFileStore,
) {

    suspend fun bytesOf(fileName: String): ByteArray? = mediaFileStore.readOrNull(fileName)

    fun pathOf(fileName: String): String = mediaFileStore.destinationFor(fileName).path
}
