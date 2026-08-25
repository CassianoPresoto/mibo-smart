package intelbras.mobi.smart.ui.token

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TokenEntryRoute(viewModel: TokenEntryViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TokenEntryScreen(
        uiState = uiState,
        onTokenChanged = viewModel::onTokenChanged,
        onSubmit = viewModel::onSubmit,
        onSignOut = viewModel::onSignOut,
    )
}
