package intelbras.mobi.smart.persistence.capture

import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import intelbras.mobi.smart.persistence.MEDIA_CAPTURE_DIRECTORY
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class FileMediaFileStore(
    rootDirectory: File,
    private val ioDispatcher: CoroutineDispatcher,
) : MediaFileStore {

    private val directory = File(rootDirectory, MEDIA_CAPTURE_DIRECTORY)

    override fun destinationFor(fileName: String): MediaFileDestination {
        directory.mkdirs()
        return MediaFileDestination(directoryPath = directory.absolutePath, fileName = fileName)
    }

    override suspend fun readOrNull(fileName: String): ByteArray? = withContext(ioDispatcher) {
        File(directory, fileName).takeIf { file -> file.exists() }?.readBytes()
    }

    override suspend fun sizeOf(fileName: String): Long = withContext(ioDispatcher) {
        File(directory, fileName).length()
    }

    override suspend fun delete(fileName: String) {
        withContext(ioDispatcher) { File(directory, fileName).delete() }
    }
}
