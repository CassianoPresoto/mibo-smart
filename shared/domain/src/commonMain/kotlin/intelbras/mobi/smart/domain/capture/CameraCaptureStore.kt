package intelbras.mobi.smart.domain.capture

import intelbras.mobi.smart.domain.capture.model.CameraCapture
import kotlinx.coroutines.flow.Flow

interface CameraCaptureStore {
    fun capturesOf(deviceSerialNumber: String): Flow<List<CameraCapture>>

    suspend fun save(capture: CameraCapture)

    suspend fun remove(captureId: String)
}
