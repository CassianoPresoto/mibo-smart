package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.capture.CameraCaptureStore
import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import kotlin.coroutines.cancellation.CancellationException

internal class CaptureRemoval(
    private val captureStore: CameraCaptureStore,
    private val mediaFileStore: MediaFileStore,
) {

    suspend operator fun invoke(capture: CameraCapture): CaptureRemovalResult = try {
        erase(capture)
        CaptureRemovalResult.Removed
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        CaptureRemovalResult.Failed(failure)
    }

    private suspend fun erase(capture: CameraCapture) {
        mediaFileStore.delete(capture.fileName)
        if (capture.previewFileName != capture.fileName && capture.previewFileName.isNotEmpty()) {
            mediaFileStore.delete(capture.previewFileName)
        }
        captureStore.remove(capture.id)
    }
}
