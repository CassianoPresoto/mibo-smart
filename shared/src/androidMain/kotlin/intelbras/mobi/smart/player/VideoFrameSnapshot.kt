package intelbras.mobi.smart.player

import android.graphics.Bitmap
import android.view.TextureView
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val JPEG_QUALITY = 92

internal class VideoFrameSnapshot {

    suspend fun capture(
        surface: TextureView?,
        destination: MediaFileDestination,
    ): FrameCaptureResult {
        val frame = frameOf(surface) ?: return FrameCaptureResult.Unavailable
        return try {
            FrameCaptureResult.Captured(
                fileName = destination.fileName,
                sizeBytes = writeJpeg(frame, destination),
            )
        } catch (failure: Throwable) {
            FrameCaptureResult.Failed(failure)
        } finally {
            frame.recycle()
        }
    }

    private suspend fun frameOf(surface: TextureView?): Bitmap? = withContext(Dispatchers.Main) {
        surface?.takeIf { view -> view.isAvailable }?.bitmap
    }

    private suspend fun writeJpeg(frame: Bitmap, destination: MediaFileDestination): Long =
        withContext(Dispatchers.IO) {
            val file = File(destination.directoryPath, destination.fileName)
            FileOutputStream(file).use { output ->
                frame.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            file.length()
        }
}
