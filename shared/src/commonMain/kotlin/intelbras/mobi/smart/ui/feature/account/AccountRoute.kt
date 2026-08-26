package intelbras.mobi.smart.ui.feature.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AccountRoute(
    onSignedOut: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.signedOut) {
        if (uiState.signedOut) onSignedOut()
    }

    AccountScreen(
        uiState = uiState,
        onDarkThemeToggled = viewModel::onDarkThemeToggled,
        onSignOut = viewModel::onSignOut,
    )
}
