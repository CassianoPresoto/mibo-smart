package intelbras.mobi.smart.ui.feature.lock

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.ui.component.MiboBackButton
import intelbras.mobi.smart.ui.component.MiboCard
import intelbras.mobi.smart.ui.feature.lock.component.LockDial
import intelbras.mobi.smart.ui.feature.lock.component.LockMetricCard
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.lock_awaiting_confirmation
import mibosmart.shared.generated.resources.lock_back
import mibosmart.shared.generated.resources.lock_dial_description
import mibosmart.shared.generated.resources.lock_hint_tap_to_close
import mibosmart.shared.generated.resources.lock_hint_tap_to_open
import mibosmart.shared.generated.resources.lock_hint_unknown
import mibosmart.shared.generated.resources.lock_hint_waiting
import mibosmart.shared.generated.resources.lock_history_empty
import mibosmart.shared.generated.resources.lock_history_loading
import mibosmart.shared.generated.resources.lock_history_see_all
import mibosmart.shared.generated.resources.lock_history_title
import mibosmart.shared.generated.resources.lock_history_unavailable
import mibosmart.shared.generated.resources.lock_history_way_remote_app
import mibosmart.shared.generated.resources.lock_metric_battery
import mibosmart.shared.generated.resources.lock_metric_battery_value
import mibosmart.shared.generated.resources.lock_metric_remote
import mibosmart.shared.generated.resources.lock_metric_remote_off
import mibosmart.shared.generated.resources.lock_metric_remote_on
import mibosmart.shared.generated.resources.lock_metric_signal
import mibosmart.shared.generated.resources.lock_metric_signal_value
import mibosmart.shared.generated.resources.lock_metric_unavailable
import mibosmart.shared.generated.resources.lock_retry
import mibosmart.shared.generated.resources.lock_status_checking
import mibosmart.shared.generated.resources.lock_status_closed
import mibosmart.shared.generated.resources.lock_status_open
import mibosmart.shared.generated.resources.lock_status_sending
import mibosmart.shared.generated.resources.lock_status_unknown
import mibosmart.shared.generated.resources.lock_subtitle_offline
import mibosmart.shared.generated.resources.lock_subtitle_online
import mibosmart.shared.generated.resources.lock_volume_awaiting_confirmation
import mibosmart.shared.generated.resources.lock_volume_changing
import mibosmart.shared.generated.resources.lock_volume_endpoint
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

private val volumeButtonHeight = 42.dp
private val openingIconSize = 30.dp
private val dialCardPadding = 22.dp

@Composable
internal fun LockScreen(
    uiState: LockUiState,
    lockName: String,
    lockModel: String,
    onToggle: () -> Unit,
    onRetry: () -> Unit,
    onVolumeSelected: (LockVolumeLevel) -> Unit,
    onVolumeRetry: () -> Unit,
    onHistoryRetry: () -> Unit,
    onSeeAllHistory: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiboTheme.colors.background)
            .statusBarsPadding(),
    ) {
        LockHeader(lockName = lockName, subtitle = uiState.subtitle(lockModel), onLeave = onLeave)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MiboSmartSize.listPadding)
                .padding(bottom = MiboSmartSpacing.lg)
                .navigationBarsPadding(),
        ) {
            LockDialCard(uiState = uiState, onToggle = onToggle, onRetry = onRetry)
            Spacer(Modifier.height(MiboSmartSpacing.md))
            LockMetricsRow(uiState.details)
            Spacer(Modifier.height(MiboSmartSpacing.md))
            LockVolumeCard(
                uiState = uiState.volume,
                onVolumeSelected = onVolumeSelected,
                onRetry = onVolumeRetry,
            )
            Spacer(Modifier.height(MiboSmartSpacing.md))
            LockHistoryCard(
                uiState = uiState.history,
                onRetry = onHistoryRetry,
                onSeeAll = onSeeAllHistory,
            )
        }
    }
}

@Composable
private fun LockHeader(lockName: String, subtitle: String, onLeave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MiboSmartSize.listPadding,
                end = MiboSmartSize.listPadding,
                top = MiboSmartSpacing.md,
                bottom = MiboSmartSpacing.sm,
            ),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiboBackButton(description = stringResource(Res.string.lock_back), onClick = onLeave)
        Column(Modifier.weight(1f)) {
            Text(
                text = lockName,
                style = MiboTheme.typography.title,
                color = MiboTheme.colors.text,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = MiboTheme.typography.caption,
                color = MiboTheme.colors.muted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LockDialCard(uiState: LockUiState, onToggle: () -> Unit, onRetry: () -> Unit) {
    val colors = MiboTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MiboSmartShapes.panel)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.panel)
            .padding(dialCardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LockDial(
            label = stringResource(uiState.dialLabel()),
            description = stringResource(Res.string.lock_dial_description),
            open = uiState.status == LockStatus.Open,
            enabled = uiState.canSwitch,
            loading = uiState.isSwitching,
            onClick = onToggle,
        )
        Spacer(Modifier.height(MiboSmartSpacing.md))
        Text(
            text = stringResource(uiState.hint()),
            style = MiboTheme.typography.caption,
            color = colors.muted,
            textAlign = TextAlign.Center,
        )

        if (uiState.awaitingConfirmation) {
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockNote(Res.string.lock_awaiting_confirmation)
        }

        uiState.failure?.let { failure ->
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockFailureMessage(failure = failure, onRetry = onRetry, canRetry = !uiState.isSwitching)
        }
    }
}

@Composable
private fun LockMetricsRow(details: LockDetailsUiState) {
    val unavailable = stringResource(Res.string.lock_metric_unavailable)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
    ) {
        LockMetricCard(
            label = stringResource(Res.string.lock_metric_battery),
            value = details.batteryPercentage
                ?.let { stringResource(Res.string.lock_metric_battery_value, it) }
                ?: unavailable,
            modifier = Modifier.weight(1f),
        )
        LockMetricCard(
            label = stringResource(Res.string.lock_metric_signal),
            value = details.signalStrength
                ?.let { stringResource(Res.string.lock_metric_signal_value, it) }
                ?: unavailable,
            monospace = true,
            modifier = Modifier.weight(1f),
        )
        LockMetricCard(
            label = stringResource(Res.string.lock_metric_remote),
            value = details.remoteOpeningEnabled?.let { enabled ->
                if (enabled) {
                    stringResource(Res.string.lock_metric_remote_on)
                } else {
                    stringResource(Res.string.lock_metric_remote_off)
                }
            } ?: unavailable,
            valueColor = if (details.remoteOpeningEnabled == true) {
                MiboTheme.colors.primary
            } else {
                MiboTheme.colors.text
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LockVolumeCard(
    uiState: LockVolumeUiState,
    onVolumeSelected: (LockVolumeLevel) -> Unit,
    onRetry: () -> Unit,
) {
    MiboCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.lock_volume_title),
                style = MiboTheme.typography.subtitle,
                color = MiboTheme.colors.text,
            )
            Text(
                text = stringResource(uiState.currentLevelResource()),
                style = MiboTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                color = MiboTheme.colors.primary,
            )
        }
        Spacer(Modifier.height(MiboSmartSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.xs),
        ) {
            LockVolumeLevel.entries.forEach { level ->
                LockVolumeButton(
                    label = stringResource(level.labelResource()),
                    selected = level == uiState.level,
                    enabled = uiState.canChange,
                    onClick = { onVolumeSelected(level) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        uiState.level?.let { level ->
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            Text(
                text = stringResource(Res.string.lock_volume_endpoint, level.code),
                style = MiboTheme.typography.monoSmall,
                color = MiboTheme.colors.muted,
            )
        }

        if (uiState.isChanging) {
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockWaiting(stringResource(Res.string.lock_volume_changing))
        }

        if (uiState.awaitingConfirmation) {
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockNote(Res.string.lock_volume_awaiting_confirmation)
        }

        if (uiState.isRemembered) {
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockNote(Res.string.lock_volume_remembered)
        }

        uiState.failure?.let { failure ->
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockFailureMessage(failure = failure, onRetry = onRetry, canRetry = uiState.canChange)
        }
    }
}

@Composable
private fun LockVolumeButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surface,
        label = "volumeButtonBackground",
    )
    val content by animateColorAsState(
        targetValue = when {
            selected -> colors.onPrimary
            enabled -> colors.text
            else -> colors.muted
        },
        label = "volumeButtonContent",
    )
    Box(
        modifier = modifier
            .height(volumeButtonHeight)
            .clip(MiboSmartShapes.medium)
            .background(background)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.medium)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MiboTheme.typography.button.copy(fontSize = 12.5.sp),
            color = content,
            maxLines = 1,
        )
    }
}

@Composable
private fun LockHistoryCard(
    uiState: LockHistoryUiState,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
    MiboCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.lock_history_title),
                style = MiboTheme.typography.subtitle,
                color = MiboTheme.colors.text,
            )
            if (uiState.hasOpenings) {
                Text(
                    text = stringResource(Res.string.lock_history_see_all),
                    style = MiboTheme.typography.button.copy(fontSize = 12.5.sp),
                    color = MiboTheme.colors.primary,
                    modifier = Modifier
                        .clip(MiboSmartShapes.code)
                        .clickable(onClick = onSeeAll)
                        .padding(horizontal = MiboSmartSpacing.xxs),
                )
            }
        }
        Spacer(Modifier.height(MiboSmartSpacing.sm))

        when {
            uiState.isLoading -> LockNote(Res.string.lock_history_loading)
            uiState.isUnavailable -> LockNote(Res.string.lock_history_unavailable)
            uiState.isEmpty -> LockNote(Res.string.lock_history_empty)
            else -> uiState.latest.forEach { opening -> LockOpeningRow(opening) }
        }

        uiState.failure?.let { failure ->
            Spacer(Modifier.height(MiboSmartSpacing.sm))
            LockFailureMessage(failure = failure, onRetry = onRetry, canRetry = !uiState.isLoading)
        }
    }
}

@Composable
private fun LockOpeningRow(opening: LockOpeningUiModel) {
    val colors = MiboTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MiboSmartSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(openingIconSize)
                .clip(MiboSmartShapes.small)
                .background(colors.primaryTint),
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = "${opening.way.label()} · ${opening.user}",
                style = MiboTheme.typography.body.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.text,
                maxLines = 1,
            )
            Text(
                text = opening.happenedAt,
                style = MiboTheme.typography.caption,
                color = colors.muted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LockFailureMessage(failure: LockFailure, onRetry: () -> Unit, canRetry: Boolean) {
    val colors = MiboTheme.colors
    Text(
        text = stringResource(failure.messageResource()),
        style = MiboTheme.typography.caption,
        color = colors.danger,
    )
    Spacer(Modifier.height(MiboSmartSpacing.xs))
    Text(
        text = stringResource(Res.string.lock_retry),
        style = MiboTheme.typography.button.copy(fontSize = 13.sp),
        color = if (canRetry) colors.primary else colors.muted,
        modifier = Modifier
            .clip(MiboSmartShapes.code)
            .clickable(enabled = canRetry, onClick = onRetry)
            .padding(vertical = MiboSmartSpacing.xxs),
    )
}

@Composable
private fun LockNote(text: StringResource) {
    Text(
        text = stringResource(text),
        style = MiboTheme.typography.caption,
        color = MiboTheme.colors.muted,
    )
}

@Composable
private fun LockWaiting(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = MiboTheme.colors.primary,
            strokeWidth = 2.dp,
        )
        Text(
            text = message,
            style = MiboTheme.typography.caption,
            color = MiboTheme.colors.muted,
        )
    }
}

@Composable
private fun LockUiState.subtitle(lockModel: String): String = when (status) {
    LockStatus.Open, LockStatus.Closed -> stringResource(Res.string.lock_subtitle_online, lockModel)
    LockStatus.Unknown -> stringResource(Res.string.lock_subtitle_offline, lockModel)
    LockStatus.Checking -> lockModel
}

private fun LockUiState.dialLabel(): StringResource = when {
    isSwitching -> Res.string.lock_status_sending
    else -> status.labelResource()
}

private fun LockUiState.hint(): StringResource = when {
    isSwitching -> Res.string.lock_hint_waiting
    status == LockStatus.Open -> Res.string.lock_hint_tap_to_close
    status == LockStatus.Closed -> Res.string.lock_hint_tap_to_open
    status == LockStatus.Checking -> Res.string.lock_hint_waiting
    else -> Res.string.lock_hint_unknown
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
private fun LockOpeningWayUiModel.label(): String = when (this) {
    LockOpeningWayUiModel.RemoteApp -> stringResource(Res.string.lock_history_way_remote_app)
    is LockOpeningWayUiModel.Unrecognized -> name
}

private val previewOpenings = listOf(
    LockOpeningUiModel(
        id = "1",
        happenedAt = "25/08/2026 17:21",
        time = "17:21",
        user = "APP",
        way = LockOpeningWayUiModel.RemoteApp,
    ),
    LockOpeningUiModel(
        id = "2",
        happenedAt = "25/08/2026 13:21",
        time = "13:21",
        user = "Cassiano",
        way = LockOpeningWayUiModel.Unrecognized("senha"),
    ),
)

private val previewState = LockUiState(
    status = LockStatus.Closed,
    volume = LockVolumeUiState(level = LockVolumeLevel.Medium, isReading = false),
    history = LockHistoryUiState(openings = previewOpenings, isLoading = false),
    details = LockDetailsUiState(
        batteryPercentage = 98,
        signalStrength = 4,
        remoteOpeningEnabled = true,
    ),
)

@Preview
@Composable
private fun LockScreenClosedPreview() {
    PreviewScreen(previewState)
}

@Preview
@Composable
private fun LockScreenOpenPreview() {
    PreviewScreen(previewState.copy(status = LockStatus.Open))
}

@Preview
@Composable
private fun LockScreenDarkPreview() {
    MiboTheme(darkTheme = true) {
        LockScreen(
            uiState = previewState.copy(status = LockStatus.Open),
            lockName = "Porta de entrada",
            lockModel = "MFR 2020 V",
            onToggle = {},
            onRetry = {},
            onVolumeSelected = {},
            onVolumeRetry = {},
            onHistoryRetry = {},
            onSeeAllHistory = {},
            onLeave = {},
        )
    }
}

@Preview
@Composable
private fun LockScreenSwitchingPreview() {
    PreviewScreen(previewState.copy(isSwitching = true))
}

@Preview
@Composable
private fun LockScreenRememberedVolumePreview() {
    PreviewScreen(
        previewState.copy(
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
            volume = LockVolumeUiState(isReading = false),
            history = LockHistoryUiState(isLoading = false, isUnavailable = true),
        ),
    )
}

@Composable
private fun PreviewScreen(uiState: LockUiState) {
    MiboTheme {
        LockScreen(
            uiState = uiState,
            lockName = "Porta de entrada",
            lockModel = "MFR 2020 V",
            onToggle = {},
            onRetry = {},
            onVolumeSelected = {},
            onVolumeRetry = {},
            onHistoryRetry = {},
            onSeeAllHistory = {},
            onLeave = {},
        )
    }
}
