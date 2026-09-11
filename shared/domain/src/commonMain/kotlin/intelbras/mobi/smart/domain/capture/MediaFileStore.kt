package intelbras.mobi.smart.domain.capture

import intelbras.mobi.smart.domain.capture.model.MediaFileDestination

interface MediaFileStore {
    fun destinationFor(fileName: String): MediaFileDestination

    suspend fun readOrNull(fileName: String): ByteArray?

    suspend fun sizeOf(fileName: String): Long

    suspend fun delete(fileName: String)
}
