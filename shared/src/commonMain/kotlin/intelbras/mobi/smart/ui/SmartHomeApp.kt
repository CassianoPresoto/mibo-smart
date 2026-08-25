package intelbras.mobi.smart.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.ui.devices.DeviceListRoute
import intelbras.mobi.smart.ui.token.StoredSessionCheck
import intelbras.mobi.smart.ui.token.TokenEntryScreen
import intelbras.mobi.smart.ui.token.TokenEntryUiState
import intelbras.mobi.smart.ui.token.TokenEntryViewModel
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

        is TokenEntryUiState.Authenticated -> DeviceListRoute(
            onSignOut = sessionViewModel::onSignOut,
        )
    }
}
