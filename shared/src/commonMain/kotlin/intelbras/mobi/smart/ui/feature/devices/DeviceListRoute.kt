package intelbras.mobi.smart.ui.feature.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DeviceListRoute(
    onDeviceClick: (DeviceUiModel) -> Unit,
    onAccountClick: () -> Unit,
    viewModel: DeviceListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DeviceListScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onDeviceClick = onDeviceClick,
        onRetry = viewModel::onRetry,
        onLoadMore = viewModel::onLoadMore,
        onAccountClick = onAccountClick,
    )
}
