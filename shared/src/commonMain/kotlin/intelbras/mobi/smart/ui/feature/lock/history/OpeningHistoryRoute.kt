package intelbras.mobi.smart.ui.feature.lock.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.domain.device.model.DeviceReference
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun OpeningHistoryRoute(
    lock: DeviceReference,
    lockName: String,
    onLeave: () -> Unit,
    viewModel: OpeningHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(lock) { viewModel.onScreenOpened(lock) }

    OpeningHistoryScreen(
        uiState = uiState,
        lockName = lockName,
        onLoadMore = viewModel::onLoadMore,
        onRetry = viewModel::onRetry,
        onLeave = onLeave,
    )
}
