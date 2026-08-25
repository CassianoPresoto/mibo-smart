package intelbras.mobi.smart.ui.feature.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TokenEntryRoute(
    sessionExpired: Boolean,
    onAuthenticated: () -> Unit,
    viewModel: TokenEntryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    TokenEntryScreen(
        uiState = uiState,
        sessionExpired = sessionExpired,
        onTokenChanged = viewModel::onTokenChanged,
        onSubmit = viewModel::onSubmit,
    )
}
