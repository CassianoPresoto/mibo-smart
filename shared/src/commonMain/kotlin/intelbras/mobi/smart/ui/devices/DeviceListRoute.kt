package intelbras.mobi.smart.ui.devices

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DeviceListRoute(
    onDeviceSelected: (DeviceListItem) -> Unit,
    onSignOut: () -> Unit,
    viewModel: DeviceListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DeviceListScreen(
        uiState = uiState,
        onReload = viewModel::onReload,
        onDeviceSelected = onDeviceSelected,
        onSignOut = onSignOut,
    )
}
