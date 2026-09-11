package intelbras.mobi.smart.business.capture.usecase

import intelbras.mobi.smart.domain.capture.CameraCaptureStore
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlinx.coroutines.flow.Flow

internal class CaptureLibraryReading(
    private val captureStore: CameraCaptureStore,
) {

    operator fun invoke(device: DeviceReference): Flow<List<CameraCapture>> =
        captureStore.capturesOf(device.serialNumber)
}
