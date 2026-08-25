package intelbras.mobi.smart.ui.feature.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import intelbras.mobi.smart.business.usecase.SessionStatus
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SessionCheckRoute(
    onSessionOpen: () -> Unit,
    onSessionMissing: (sessionExpired: Boolean) -> Unit,
    viewModel: SessionViewModel = koinViewModel(),
) {
    val status by viewModel.status.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onSessionChecked() }
    LaunchedEffect(status) {
        when (status) {
            is SessionStatus.Active -> onSessionOpen()
            SessionStatus.Expired -> onSessionMissing(true)
            SessionStatus.None -> onSessionMissing(false)
            null -> Unit
        }
    }

    SessionCheckScreen()
}
