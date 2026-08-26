package intelbras.mobi.smart.ui.feature.lock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.domain.device.model.DeviceReference
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LockRoute(
    lock: DeviceReference,
    lockName: String,
    lockModel: String,
    onLeave: () -> Unit,
    viewModel: LockViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(lock) { viewModel.onScreenOpened(lock) }
    LifecycleResumeEffect(lock) {
        viewModel.onScreenResumed()
        onPauseOrDispose { }
    }

    LockScreen(
        uiState = uiState,
        lockName = lockName,
        lockModel = lockModel,
        onOpen = viewModel::onOpen,
        onClose = viewModel::onClose,
        onRetry = viewModel::onRetry,
        onVolumeSelected = viewModel::onVolumeSelected,
        onVolumeRetry = viewModel::onVolumeRetry,
        onLeave = onLeave,
    )
}
