package intelbras.mobi.smart.ui.feature.video.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.domain.device.model.DeviceReference
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CaptureLibraryRoute(
    camera: DeviceReference,
    cameraName: String,
    selectedCaptureId: String?,
    onLeave: () -> Unit,
    viewModel: CaptureLibraryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(camera) { viewModel.onScreenOpened(camera) }

    CaptureLibraryScreen(
        uiState = uiState,
        cameraName = cameraName,
        selectedCaptureId = selectedCaptureId,
        loadPreview = viewModel::previewOf,
        pathOf = viewModel::pathOf,
        onCaptureRemoved = viewModel::onCaptureRemoved,
        onLeave = onLeave,
    )
}
