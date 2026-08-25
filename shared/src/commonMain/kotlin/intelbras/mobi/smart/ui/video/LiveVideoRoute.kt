package intelbras.mobi.smart.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.domain.device.model.DeviceReference
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LiveVideoRoute(
    device: DeviceReference,
    deviceName: String,
    deviceModel: String,
    onLeave: () -> Unit,
    viewModel: LiveVideoViewModel = koinViewModel(),
) {
    val player = rememberVideoPlayer()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val details by viewModel.details.collectAsStateWithLifecycle()

    LaunchedEffect(device, player) {
        viewModel.onScreenOpened(device, player)
    }
    DisposableEffect(device, player) {
        onDispose { viewModel.onScreenClosed() }
    }

    LiveVideoScreen(
        uiState = uiState,
        details = details,
        player = player,
        deviceName = deviceName,
        deviceModel = deviceModel,
        deviceSerialNumber = device.serialNumber,
        onRetry = viewModel::onRetry,
        onDetailsToggled = viewModel::onDetailsToggled,
        onLeave = onLeave,
    )
}
