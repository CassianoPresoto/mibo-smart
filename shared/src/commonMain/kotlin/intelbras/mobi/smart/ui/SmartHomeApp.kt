package intelbras.mobi.smart.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.ui.devices.DeviceListItem
import intelbras.mobi.smart.ui.devices.DeviceListRoute
import intelbras.mobi.smart.ui.token.StoredSessionCheck
import intelbras.mobi.smart.ui.token.TokenEntryScreen
import intelbras.mobi.smart.ui.token.TokenEntryUiState
import intelbras.mobi.smart.ui.token.TokenEntryViewModel
import intelbras.mobi.smart.ui.video.LiveVideoRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SmartHomeApp(sessionViewModel: TokenEntryViewModel = koinViewModel()) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    when (val state = sessionState) {
        TokenEntryUiState.CheckingStoredSession -> StoredSessionCheck()

        is TokenEntryUiState.AwaitingToken -> TokenEntryScreen(
            uiState = state,
            onTokenChanged = sessionViewModel::onTokenChanged,
            onSubmit = sessionViewModel::onSubmit,
        )

        is TokenEntryUiState.Authenticated -> SignedInApp(
            onSignOut = sessionViewModel::onSignOut,
        )
    }
}

@Composable
private fun SignedInApp(onSignOut: () -> Unit) {
    var watchedDevice by remember { mutableStateOf<DeviceListItem?>(null) }

    when (val device = watchedDevice) {
        null -> DeviceListRoute(
            onDeviceSelected = { watchedDevice = it },
            onSignOut = onSignOut,
        )

        else -> LiveVideoRoute(
            device = DeviceReference(
                serialNumber = device.serialNumber,
                productId = device.productId,
            ),
            deviceName = device.name,
            onLeave = { watchedDevice = null },
        )
    }
}
