package intelbras.mobi.smart.ui.feature.activity

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.feature.lock.LockFailure
import intelbras.mobi.smart.ui.feature.lock.LockOpeningWayUiModel
import intelbras.mobi.smart.ui.feature.lock.history.OpeningDayLabel
import intelbras.mobi.smart.ui.feature.lock.messageResource
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.activity_empty
import mibosmart.shared.generated.resources.activity_loading
import mibosmart.shared.generated.resources.activity_no_locks
import mibosmart.shared.generated.resources.activity_subtitle
import mibosmart.shared.generated.resources.activity_title
import mibosmart.shared.generated.resources.activity_unavailable
import mibosmart.shared.generated.resources.lock_history_way_remote_app
import mibosmart.shared.generated.resources.lock_retry
import mibosmart.shared.generated.resources.opening_history_today
import mibosmart.shared.generated.resources.opening_history_undated
import mibosmart.shared.generated.resources.opening_history_yesterday
import org.jetbrains.compose.resources.stringResource

private val markerSize = 34.dp
private val markerDotSize = 12.dp

@Composable
internal fun ActivityScreen(
    uiState: ActivityUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiboTheme.colors.background)
            .statusBarsPadding(),
    ) {
        ActivityHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MiboSmartSize.listPadding)
                .padding(bottom = MiboSmartSpacing.lg),
        ) {
            when {
                uiState.isLoading -> ActivityNote(stringResource(Res.string.activity_loading))
                uiState.hasNoLocks -> ActivityNote(stringResource(Res.string.activity_no_locks))
                uiState.isUnavailable -> ActivityNote(stringResource(Res.string.activity_unavailable))
                uiState.isEmpty -> ActivityNote(stringResource(Res.string.activity_empty))
                else -> uiState.days.forEach { day -> ActivityDaySection(day) }
            }

            uiState.failure?.let { failure -> ActivityFailure(failure = failure, onRetry = onRetry) }
        }
    }
}

@Composable
private fun ActivityHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MiboSmartSize.listPadding,
                vertical = MiboSmartSpacing.md,
            ),
    ) {
        Text(
            text = stringResource(Res.string.activity_title),
            style = MiboTheme.typography.display,
            color = MiboTheme.colors.text,
        )
        Text(
            text = stringResource(Res.string.activity_subtitle),
            style = MiboTheme.typography.caption,
            color = MiboTheme.colors.muted,
        )
    }
}

@Composable
private fun ActivityDaySection(day: ActivityDayUiModel) {
    val colors = MiboTheme.colors
    Text(
        text = day.label.text().uppercase(),
        style = MiboTheme.typography.label.copy(letterSpacing = 0.9.sp),
        color = colors.muted,
        modifier = Modifier.padding(top = MiboSmartSpacing.md, bottom = MiboSmartSpacing.xs),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MiboSmartShapes.icon)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.icon),
    ) {
        day.entries.forEachIndexed { position, entry ->
            if (position > 0) HorizontalDivider(color = colors.outline)
            ActivityRow(entry)
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntryUiModel) {
    val colors = MiboTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MiboSmartSpacing.md, vertical = MiboSmartSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(markerSize)
                .clip(MiboSmartShapes.small)
                .background(colors.primaryTint),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(markerDotSize)
                    .clip(CircleShape)
                    .background(colors.primary),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.lockName,
                style = MiboTheme.typography.body.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.text,
                maxLines = 1,
            )
            Text(
                text = "${entry.way.label()} · ${entry.user}",
                style = MiboTheme.typography.caption,
                color = colors.muted,
                maxLines = 1,
            )
        }
        Text(
            text = entry.time,
            style = MiboTheme.typography.monoSmall.copy(fontSize = 12.sp),
            color = colors.muted,
        )
    }
}

@Composable
private fun ActivityNote(text: String) {
    Text(
        text = text,
        style = MiboTheme.typography.caption,
        color = MiboTheme.colors.muted,
        modifier = Modifier.padding(top = MiboSmartSpacing.md),
    )
}

@Composable
private fun ActivityFailure(failure: LockFailure, onRetry: () -> Unit) {
    Spacer(Modifier.height(MiboSmartSpacing.md))
    Text(
        text = stringResource(failure.messageResource()),
        style = MiboTheme.typography.caption,
        color = MiboTheme.colors.danger,
    )
    Spacer(Modifier.height(MiboSmartSpacing.xs))
    Text(
        text = stringResource(Res.string.lock_retry),
        style = MiboTheme.typography.button.copy(fontSize = 13.sp),
        color = MiboTheme.colors.primary,
        modifier = Modifier
            .clip(MiboSmartShapes.code)
            .clickable(onClick = onRetry)
            .padding(vertical = MiboSmartSpacing.xxs),
    )
}

@Composable
private fun OpeningDayLabel.text(): String = when (this) {
    OpeningDayLabel.Today -> stringResource(Res.string.opening_history_today)
    OpeningDayLabel.Yesterday -> stringResource(Res.string.opening_history_yesterday)
    OpeningDayLabel.Undated -> stringResource(Res.string.opening_history_undated)
    is OpeningDayLabel.Day -> date
}

@Composable
private fun LockOpeningWayUiModel.label(): String = when (this) {
    LockOpeningWayUiModel.RemoteApp -> stringResource(Res.string.lock_history_way_remote_app)
    is LockOpeningWayUiModel.Unrecognized -> name
}

private fun entry(id: String, lockName: String, time: String, user: String) = ActivityEntryUiModel(
    id = id,
    lockName = lockName,
    time = time,
    user = user,
    way = LockOpeningWayUiModel.RemoteApp,
)

private val previewState = ActivityUiState(
    days = listOf(
        ActivityDayUiModel(
            label = OpeningDayLabel.Today,
            entries = listOf(
                entry("1", "Porta de entrada", "08:12", "APP"),
                entry("2", "Portão da garagem", "07:38", "Marina"),
            ),
        ),
        ActivityDayUiModel(
            label = OpeningDayLabel.Yesterday,
            entries = listOf(entry("3", "Porta de entrada", "19:47", "APP")),
        ),
    ),
    isLoading = false,
)

@Preview
@Composable
private fun ActivityScreenPreview() {
    MiboTheme {
        ActivityScreen(uiState = previewState, onRetry = {})
    }
}

@Preview
@Composable
private fun ActivityScreenDarkPreview() {
    MiboTheme(darkTheme = true) {
        ActivityScreen(uiState = previewState, onRetry = {})
    }
}

@Preview
@Composable
private fun ActivityScreenWithoutLocksPreview() {
    MiboTheme {
        ActivityScreen(
            uiState = ActivityUiState(isLoading = false, hasNoLocks = true),
            onRetry = {},
        )
    }
}
