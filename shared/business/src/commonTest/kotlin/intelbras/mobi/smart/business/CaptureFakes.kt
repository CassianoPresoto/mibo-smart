package intelbras.mobi.smart.business

import intelbras.mobi.smart.domain.capture.CameraCaptureStore
import intelbras.mobi.smart.domain.capture.LiveClipRecorder
import intelbras.mobi.smart.domain.capture.LiveFrameCapture
import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal const val MEDIA_DIRECTORY = "/media/mibo-captures"
internal const val CAPTURED_SIZE_BYTES = 2_048L

internal class InMemoryMediaFileStore : MediaFileStore {

    val deleted = mutableListOf<String>()

    override fun destinationFor(fileName: String) =
        MediaFileDestination(directoryPath = MEDIA_DIRECTORY, fileName = fileName)

    override suspend fun readOrNull(fileName: String): ByteArray? = null

    override suspend fun sizeOf(fileName: String): Long = CAPTURED_SIZE_BYTES

    override suspend fun delete(fileName: String) {
        deleted += fileName
    }
}

internal class InMemoryCameraCaptureStore : CameraCaptureStore {

    private val stored = MutableStateFlow(emptyList<CameraCapture>())

    val saved: List<CameraCapture> get() = stored.value
    val removed = mutableListOf<String>()

    override fun capturesOf(deviceSerialNumber: String): Flow<List<CameraCapture>> =
        stored.asStateFlow()

    override suspend fun save(capture: CameraCapture) {
        stored.value = stored.value + capture
    }

    override suspend fun remove(captureId: String) {
        removed += captureId
        stored.value = stored.value.filterNot { capture -> capture.id == captureId }
    }
}

internal class FakeFrameCapture(
    private val result: FrameCaptureResult = FrameCaptureResult.Captured("", CAPTURED_SIZE_BYTES),
) : LiveFrameCapture {

    val destinations = mutableListOf<MediaFileDestination>()

    override suspend fun captureFrame(destination: MediaFileDestination): FrameCaptureResult {
        destinations += destination
        return when (result) {
            is FrameCaptureResult.Captured -> result.copy(fileName = destination.fileName)
            else -> result
        }
    }
}

internal class FakeClipRecorder(
    private val start: ClipRecordingStart = ClipRecordingStart.Started,
    private val outcome: ClipRecordingOutcome = ClipRecordingOutcome.Recorded(
        fileName = "take.mp4",
        sizeBytes = CAPTURED_SIZE_BYTES,
    ),
) : LiveClipRecorder {

    val destinations = mutableListOf<MediaFileDestination>()
    var finished = 0
        private set

    override suspend fun startRecording(destination: MediaFileDestination): ClipRecordingStart {
        destinations += destination
        return start
    }

    override suspend fun finishRecording(): ClipRecordingOutcome {
        finished++
        return outcome
    }
}
