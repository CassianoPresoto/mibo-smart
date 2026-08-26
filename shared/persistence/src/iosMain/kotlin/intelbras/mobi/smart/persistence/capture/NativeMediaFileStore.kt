package intelbras.mobi.smart.persistence.capture

import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import intelbras.mobi.smart.persistence.MEDIA_CAPTURE_DIRECTORY
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.posix.memcpy

private const val NO_SIZE = 0L

@OptIn(ExperimentalForeignApi::class)
internal class NativeMediaFileStore(
    private val ioDispatcher: CoroutineDispatcher,
) : MediaFileStore {

    private val fileManager = NSFileManager.defaultManager

    private val directoryPath: String = documentsPath() + "/" + MEDIA_CAPTURE_DIRECTORY

    override fun destinationFor(fileName: String): MediaFileDestination {
        fileManager.createDirectoryAtPath(directoryPath, withIntermediateDirectories = true, attributes = null, error = null)
        return MediaFileDestination(directoryPath = directoryPath, fileName = fileName)
    }

    override suspend fun readOrNull(fileName: String): ByteArray? = withContext(ioDispatcher) {
        val data = fileManager.contentsAtPath(pathOf(fileName)) ?: return@withContext null
        val length = data.length.toInt()
        ByteArray(length).apply {
            if (length == 0) return@apply
            usePinned { pinned -> memcpy(pinned.addressOf(0), data.bytes, data.length) }
        }
    }

    override suspend fun sizeOf(fileName: String): Long = withContext(ioDispatcher) {
        val attributes = fileManager.attributesOfItemAtPath(pathOf(fileName), error = null)
        (attributes?.get(NSFileSize) as? Number)?.toLong() ?: NO_SIZE
    }

    override suspend fun delete(fileName: String) {
        withContext(ioDispatcher) { fileManager.removeItemAtPath(pathOf(fileName), error = null) }
    }

    private fun pathOf(fileName: String): String = "$directoryPath/$fileName"

    private fun documentsPath(): String =
        NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .first() as String
}
