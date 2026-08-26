package intelbras.mobi.smart.ui.feature.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.lock_awaiting_confirmation
import mibosmart.shared.generated.resources.lock_back
import mibosmart.shared.generated.resources.lock_close
import mibosmart.shared.generated.resources.lock_history_empty
import mibosmart.shared.generated.resources.lock_history_loading
import mibosmart.shared.generated.resources.lock_history_title
import mibosmart.shared.generated.resources.lock_history_unavailable
import mibosmart.shared.generated.resources.lock_history_way_remote_app
import mibosmart.shared.generated.resources.lock_open
import mibosmart.shared.generated.resources.lock_retry
import mibosmart.shared.generated.resources.lock_status_checking
import mibosmart.shared.generated.resources.lock_status_closed
import mibosmart.shared.generated.resources.lock_status_open
import mibosmart.shared.generated.resources.lock_status_title
import mibosmart.shared.generated.resources.lock_status_unknown
import mibosmart.shared.generated.resources.lock_switching
import mibosmart.shared.generated.resources.lock_volume_awaiting_confirmation
import mibosmart.shared.generated.resources.lock_volume_changing
import mibosmart.shared.generated.resources.lock_volume_high
import mibosmart.shared.generated.resources.lock_volume_low
import mibosmart.shared.generated.resources.lock_volume_medium
import mibosmart.shared.generated.resources.lock_volume_mute
import mibosmart.shared.generated.resources.lock_volume_reading
import mibosmart.shared.generated.resources.lock_volume_remembered
import mibosmart.shared.generated.resources.lock_volume_title
import mibosmart.shared.generated.resources.lock_volume_unknown
import org.jetbrains.compose.resources.StringResource
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
    onVolumeSelected: (LockVolumeLevel) -> Unit,
    onVolumeRetry: () -> Unit,
    onHistoryRetry: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            LockStatusCard(uiState = uiState, lockModel = lockModel, onRetry = onRetry)
            Spacer(Modifier.height(24.dp))
            LockControls(uiState = uiState, onOpen = onOpen, onClose = onClose)
            Spacer(Modifier.height(24.dp))
            LockVolumeCard(
                uiState = uiState.volume,
                onVolumeSelected = onVolumeSelected,
                onRetry = onVolumeRetry,
            )
            Spacer(Modifier.height(24.dp))
            LockHistoryCard(uiState = uiState.history, onRetry = onHistoryRetry)
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
                LockFailureMessage(failure = failure, onRetry = onRetry, canRetry = !uiState.isSwitching)
            }
        }
    }
}

@Composable
private fun LockControls(uiState: LockUiState, onOpen: () -> Unit, onClose: () -> Unit) {
    if (uiState.isSwitching) {
        LockWaiting(message = stringResource(Res.string.lock_switching))
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

@Composable
private fun LockVolumeCard(
    uiState: LockVolumeUiState,
    onVolumeSelected: (LockVolumeLevel) -> Unit,
    onRetry: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.lock_volume_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(uiState.currentLevelResource()),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(Modifier.height(12.dp))
            LockVolumeChoices(uiState = uiState, onVolumeSelected = onVolumeSelected)

            if (uiState.isChanging) {
                Spacer(Modifier.height(12.dp))
                LockWaiting(message = stringResource(Res.string.lock_volume_changing))
            }

            if (uiState.awaitingConfirmation) {
                Spacer(Modifier.height(12.dp))
                LockVolumeNote(Res.string.lock_volume_awaiting_confirmation)
            }

            if (uiState.isRemembered) {
                Spacer(Modifier.height(12.dp))
                LockVolumeNote(Res.string.lock_volume_remembered)
            }

            uiState.failure?.let { failure ->
                Spacer(Modifier.height(12.dp))
                LockFailureMessage(failure = failure, onRetry = onRetry, canRetry = uiState.canChange)
            }
        }
    }
}

@Composable
private fun LockHistoryCard(uiState: LockHistoryUiState, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.lock_history_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> LockVolumeNote(Res.string.lock_history_loading)
                uiState.isUnavailable -> LockVolumeNote(Res.string.lock_history_unavailable)
                uiState.isEmpty -> LockVolumeNote(Res.string.lock_history_empty)
                else -> LockOpeningList(uiState.openings)
            }

            uiState.failure?.let { failure ->
                Spacer(Modifier.height(12.dp))
                LockFailureMessage(
                    failure = failure,
                    onRetry = onRetry,
                    canRetry = !uiState.isLoading,
                )
            }
        }
    }
}

@Composable
private fun LockOpeningList(openings: List<LockOpeningUiModel>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        openings.forEachIndexed { position, opening ->
            if (position > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            LockOpeningRow(opening)
        }
    }
}

@Composable
private fun LockOpeningRow(opening: LockOpeningUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = opening.happenedAt,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = opening.way.label(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = opening.user,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LockOpeningWayUiModel.label(): String = when (this) {
    LockOpeningWayUiModel.RemoteApp -> stringResource(Res.string.lock_history_way_remote_app)
    is LockOpeningWayUiModel.Unrecognized -> name
}

@Composable
private fun LockVolumeChoices(
    uiState: LockVolumeUiState,
    onVolumeSelected: (LockVolumeLevel) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LockVolumeLevel.entries.forEach { level ->
            FilterChip(
                selected = level == uiState.level,
                onClick = { onVolumeSelected(level) },
                enabled = uiState.canChange,
                label = { Text(stringResource(level.labelResource())) },
            )
        }
    }
}

@Composable
private fun LockVolumeNote(text: StringResource) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LockFailureMessage(failure: LockFailure, onRetry: () -> Unit, canRetry: Boolean) {
    Text(
        text = stringResource(failure.messageResource()),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onRetry, enabled = canRetry) {
        Text(stringResource(Res.string.lock_retry))
    }
}

@Composable
private fun LockWaiting(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private fun LockVolumeUiState.currentLevelResource(): StringResource = when {
    isReading -> Res.string.lock_volume_reading
    level == null -> Res.string.lock_volume_unknown
    else -> level.labelResource()
}

private fun LockVolumeLevel.labelResource() = when (this) {
    LockVolumeLevel.Mute -> Res.string.lock_volume_mute
    LockVolumeLevel.Low -> Res.string.lock_volume_low
    LockVolumeLevel.Medium -> Res.string.lock_volume_medium
    LockVolumeLevel.High -> Res.string.lock_volume_high
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
    PreviewScreen(
        LockUiState(
            status = LockStatus.Closed,
            volume = LockVolumeUiState(level = LockVolumeLevel.Medium, isReading = false),
        ),
    )
}

@Preview
@Composable
private fun LockScreenWithHistoryPreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Closed,
            volume = LockVolumeUiState(level = LockVolumeLevel.Medium, isReading = false),
            history = LockHistoryUiState(
                openings = listOf(
                    LockOpeningUiModel(
                        id = "1",
                        happenedAt = "25/08/2026 17:21",
                        user = "APP",
                        way = LockOpeningWayUiModel.RemoteApp,
                    ),
                    LockOpeningUiModel(
                        id = "2",
                        happenedAt = "25/08/2026 13:21",
                        user = "Cassiano",
                        way = LockOpeningWayUiModel.Unrecognized("senha"),
                    ),
                ),
                isLoading = false,
            ),
        ),
    )
}

@Preview
@Composable
private fun LockScreenEmptyHistoryPreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Closed,
            volume = LockVolumeUiState(level = LockVolumeLevel.Mute, isReading = false),
            history = LockHistoryUiState(isLoading = false),
        ),
    )
}

@Preview
@Composable
private fun LockScreenOpenPreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Open,
            volume = LockVolumeUiState(level = LockVolumeLevel.High, isReading = false),
        ),
    )
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
private fun LockScreenChangingVolumePreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Closed,
            volume = LockVolumeUiState(
                level = LockVolumeLevel.Low,
                isReading = false,
                isChanging = true,
            ),
        ),
    )
}

@Preview
@Composable
private fun LockScreenVolumeUnreadablePreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Closed,
            volume = LockVolumeUiState(isReading = false, failure = LockFailure.NetworkUnavailable),
        ),
    )
}

@Preview
@Composable
private fun LockScreenVolumeRememberedPreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Closed,
            volume = LockVolumeUiState(
                level = LockVolumeLevel.Medium,
                source = LockVolumeSource.Remembered,
                isReading = false,
            ),
        ),
    )
}

@Preview
@Composable
private fun LockScreenOfflinePreview() {
    PreviewScreen(
        LockUiState(
            status = LockStatus.Unknown,
            failure = LockFailure.DeviceOffline,
            volume = LockVolumeUiState(isReading = false, failure = LockFailure.DeviceOffline),
        ),
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
                onVolumeSelected = {},
                onVolumeRetry = {},
                onHistoryRetry = {},
                onLeave = {},
            )
        }
    }
}
