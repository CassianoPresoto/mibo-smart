package intelbras.mobi.smart.ui.feature.video.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.capture.CameraCaptures
import intelbras.mobi.smart.domain.capture.model.CameraCapture
import intelbras.mobi.smart.domain.device.model.DeviceReference
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class CaptureLibraryViewModel(
    private val cameraCaptures: CameraCaptures,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(CaptureLibraryUiState())
    val uiState: StateFlow<CaptureLibraryUiState> = mutableUiState.asStateFlow()

    private var saved: List<CameraCapture> = emptyList()
    private var library: Job? = null

    fun onScreenOpened(device: DeviceReference) {
        library?.cancel()
        library = viewModelScope.launch {
            cameraCaptures.capturesOf(device).collect { captures ->
                saved = captures
                mutableUiState.value = CaptureLibraryUiState(
                    isLoading = false,
                    captures = captures.toUiModels(TimeZone.currentSystemDefault()),
                )
            }
        }
    }

    fun onCaptureRemoved(captureId: String) {
        val capture = saved.firstOrNull { saved -> saved.id == captureId } ?: return
        viewModelScope.launch { cameraCaptures.remove(capture) }
    }

    suspend fun previewOf(fileName: String): ByteArray? = cameraCaptures.mediaBytesOf(fileName)

    fun pathOf(fileName: String): String = cameraCaptures.mediaPathOf(fileName)
}
