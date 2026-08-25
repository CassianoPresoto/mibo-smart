package intelbras.mobi.smart.ui.feature.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.lock_back
import mibosmart.shared.generated.resources.lock_close
import mibosmart.shared.generated.resources.lock_open
import mibosmart.shared.generated.resources.lock_retry
import mibosmart.shared.generated.resources.lock_status_checking
import mibosmart.shared.generated.resources.lock_status_closed
import mibosmart.shared.generated.resources.lock_status_open
import mibosmart.shared.generated.resources.lock_status_title
import mibosmart.shared.generated.resources.lock_status_unknown
import mibosmart.shared.generated.resources.lock_awaiting_confirmation
import mibosmart.shared.generated.resources.lock_switching
import org.jetbrains.compose.resources.stringResource

private val statusDotSize = 12.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LockScreen(
    uiState: LockUiState,
    lockName: String,
    lockModel: String,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(lockName) },
                navigationIcon = {
                    TextButton(onClick = onLeave) { Text(stringResource(Res.string.lock_back)) }
                },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(16.dp),
        ) {
            LockStatusCard(uiState = uiState, lockModel = lockModel, onRetry = onRetry)
            Spacer(Modifier.height(24.dp))
            LockControls(uiState = uiState, onOpen = onOpen, onClose = onClose)
        }
    }
}

@Composable
private fun LockStatusCard(uiState: LockUiState, lockModel: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.lock_status_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(statusDotSize)
                        .clip(CircleShape)
                        .background(uiState.status.color()),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(uiState.status.labelResource()),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = lockModel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (uiState.awaitingConfirmation) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.lock_awaiting_confirmation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            uiState.failure?.let { failure ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(failure.messageResource()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRetry, enabled = !uiState.isSwitching) {
                    Text(stringResource(Res.string.lock_retry))
                }
            }
        }
    }
}

@Composable
private fun LockControls(uiState: LockUiState, onOpen: () -> Unit, onClose: () -> Unit) {
    if (uiState.isSwitching) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.lock_switching),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onOpen,
            modifier = Modifier.weight(1f),
            enabled = uiState.canSwitch && uiState.status != LockStatus.Open,
        ) {
            Text(stringResource(Res.string.lock_open))
        }
        OutlinedButton(
            onClick = onClose,
            modifier = Modifier.weight(1f),
            enabled = uiState.canSwitch && uiState.status != LockStatus.Closed,
        ) {
            Text(stringResource(Res.string.lock_close))
        }
    }
}

private fun LockStatus.labelResource() = when (this) {
    LockStatus.Checking -> Res.string.lock_status_checking
    LockStatus.Open -> Res.string.lock_status_open
    LockStatus.Closed -> Res.string.lock_status_closed
    LockStatus.Unknown -> Res.string.lock_status_unknown
}

@Composable
private fun LockStatus.color(): Color = when (this) {
    LockStatus.Open -> MaterialTheme.colorScheme.error
    LockStatus.Closed -> MaterialTheme.colorScheme.primary
    LockStatus.Checking, LockStatus.Unknown -> MaterialTheme.colorScheme.outline
}

@Preview
@Composable
private fun LockScreenClosedPreview() {
    PreviewScreen(LockUiState(status = LockStatus.Closed))
}

@Preview
@Composable
private fun LockScreenOpenPreview() {
    PreviewScreen(LockUiState(status = LockStatus.Open))
}

@Preview
@Composable
private fun LockScreenCheckingPreview() {
    PreviewScreen(LockUiState())
}

@Preview
@Composable
private fun LockScreenSwitchingPreview() {
    PreviewScreen(LockUiState(status = LockStatus.Closed, isSwitching = true))
}

@Preview
@Composable
private fun LockScreenAwaitingConfirmationPreview() {
    PreviewScreen(LockUiState(status = LockStatus.Closed, awaitingConfirmation = true))
}

@Preview
@Composable
private fun LockScreenOfflinePreview() {
    PreviewScreen(
        LockUiState(status = LockStatus.Unknown, failure = LockFailure.DeviceOffline),
    )
}

@Composable
private fun PreviewScreen(uiState: LockUiState) {
    MaterialTheme {
        Surface {
            LockScreen(
                uiState = uiState,
                lockName = "MFR 2020 V-116A",
                lockModel = "MFR 2020 V",
                onOpen = {},
                onClose = {},
                onRetry = {},
                onLeave = {},
            )
        }
    }
}
