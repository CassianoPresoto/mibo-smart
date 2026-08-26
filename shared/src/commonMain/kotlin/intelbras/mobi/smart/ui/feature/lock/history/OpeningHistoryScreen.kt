package intelbras.mobi.smart.ui.feature.lock.history

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.component.MiboBackButton
import intelbras.mobi.smart.ui.feature.lock.LockFailure
import intelbras.mobi.smart.ui.feature.lock.LockOpeningUiModel
import intelbras.mobi.smart.ui.feature.lock.LockOpeningWayUiModel
import intelbras.mobi.smart.ui.feature.lock.messageResource
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.lock_history_way_remote_app
import mibosmart.shared.generated.resources.lock_retry
import mibosmart.shared.generated.resources.opening_history_back
import mibosmart.shared.generated.resources.opening_history_empty
import mibosmart.shared.generated.resources.opening_history_load_more
import mibosmart.shared.generated.resources.opening_history_loading
import mibosmart.shared.generated.resources.opening_history_subtitle
import mibosmart.shared.generated.resources.opening_history_title
import mibosmart.shared.generated.resources.opening_history_today
import mibosmart.shared.generated.resources.opening_history_undated
import mibosmart.shared.generated.resources.opening_history_unavailable
import mibosmart.shared.generated.resources.opening_history_yesterday
import org.jetbrains.compose.resources.stringResource

private val markerSize = 34.dp
private val markerDotSize = 12.dp
private val loadMoreSpinnerSize = 18.dp

@Composable
internal fun OpeningHistoryScreen(
    uiState: OpeningHistoryUiState,
    lockName: String,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiboTheme.colors.background)
            .statusBarsPadding(),
    ) {
        OpeningHistoryHeader(lockName = lockName, onLeave = onLeave)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MiboSmartSize.listPadding)
                .padding(bottom = MiboSmartSpacing.lg)
                .navigationBarsPadding(),
        ) {
            when {
                uiState.isLoading -> OpeningHistoryNote(stringResource(Res.string.opening_history_loading))
                uiState.isUnavailable ->
                    OpeningHistoryNote(stringResource(Res.string.opening_history_unavailable))

                uiState.isEmpty -> OpeningHistoryNote(stringResource(Res.string.opening_history_empty))
                else -> uiState.days.forEach { day -> OpeningDaySection(day) }
            }

            uiState.failure?.let { failure -> OpeningHistoryFailure(failure = failure, onRetry = onRetry) }

            if (uiState.canLoadMore || uiState.isLoadingMore) {
                LoadMore(loading = uiState.isLoadingMore, onLoadMore = onLoadMore)
            }
        }
    }
}

@Composable
private fun OpeningHistoryHeader(lockName: String, onLeave: () -> Unit) {
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
        MiboBackButton(
            description = stringResource(Res.string.opening_history_back),
            onClick = onLeave,
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.opening_history_title),
                style = MiboTheme.typography.title,
                color = MiboTheme.colors.text,
                maxLines = 1,
            )
            Text(
                text = stringResource(Res.string.opening_history_subtitle, lockName),
                style = MiboTheme.typography.caption,
                color = MiboTheme.colors.muted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun OpeningDaySection(day: OpeningDayUiModel) {
    val colors = MiboTheme.colors
    Text(
        text = day.label.text().uppercase(),
        style = MiboTheme.typography.label.copy(letterSpacing = 0.9.sp),
        color = colors.muted,
        modifier = Modifier.padding(
            top = MiboSmartSpacing.md,
            bottom = MiboSmartSpacing.xs,
        ),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MiboSmartShapes.icon)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.icon),
    ) {
        day.openings.forEachIndexed { position, opening ->
            if (position > 0) HorizontalDivider(color = colors.outline)
            OpeningRow(opening)
        }
    }
}

@Composable
private fun OpeningRow(opening: LockOpeningUiModel) {
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
                text = opening.way.label(),
                style = MiboTheme.typography.body.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.text,
                maxLines = 1,
            )
            Text(
                text = opening.user,
                style = MiboTheme.typography.caption,
                color = colors.muted,
                maxLines = 1,
            )
        }
        Text(
            text = opening.time,
            style = MiboTheme.typography.monoSmall.copy(fontSize = 12.sp),
            color = colors.muted,
        )
    }
}

@Composable
private fun LoadMore(loading: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = MiboSmartSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(loadMoreSpinnerSize),
                color = MiboTheme.colors.primary,
                strokeWidth = 2.dp,
            )
            return@Box
        }
        Text(
            text = stringResource(Res.string.opening_history_load_more),
            style = MiboTheme.typography.button.copy(fontSize = 12.5.sp),
            color = MiboTheme.colors.primary,
            modifier = Modifier
                .clip(MiboSmartShapes.code)
                .clickable(onClick = onLoadMore)
                .padding(horizontal = MiboSmartSpacing.sm, vertical = MiboSmartSpacing.xxs),
        )
    }
}

@Composable
private fun OpeningHistoryNote(text: String) {
    Text(
        text = text,
        style = MiboTheme.typography.caption,
        color = MiboTheme.colors.muted,
        modifier = Modifier.padding(top = MiboSmartSpacing.md),
    )
}

@Composable
private fun OpeningHistoryFailure(failure: LockFailure, onRetry: () -> Unit) {
    Spacer(Modifier.height(MiboSmartSpacing.md))
    Text(
        text = stringResource(failure.messageResource()),
        style = MiboTheme.typography.caption,
        color = MiboTheme.colors.danger,
        textAlign = TextAlign.Start,
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

private fun opening(id: String, time: String, user: String, way: LockOpeningWayUiModel) =
    LockOpeningUiModel(
        id = id,
        happenedAt = "25/08/2026 $time",
        time = time,
        user = user,
        way = way,
    )

private val previewState = OpeningHistoryUiState(
    days = listOf(
        OpeningDayUiModel(
            label = OpeningDayLabel.Today,
            openings = listOf(
                opening("1", "08:12", "Rafael", LockOpeningWayUiModel.Unrecognized("Digital")),
                opening("2", "07:38", "Marina", LockOpeningWayUiModel.Unrecognized("Senha")),
            ),
        ),
        OpeningDayUiModel(
            label = OpeningDayLabel.Yesterday,
            openings = listOf(
                opening("3", "19:47", "APP", LockOpeningWayUiModel.RemoteApp),
                opening("4", "14:22", "Visitante", LockOpeningWayUiModel.Unrecognized("Cartão")),
            ),
        ),
    ),
    isLoading = false,
    canLoadMore = true,
)

@Preview
@Composable
private fun OpeningHistoryScreenPreview() {
    MiboTheme {
        OpeningHistoryScreen(
            uiState = previewState,
            lockName = "Porta de entrada",
            onLoadMore = {},
            onRetry = {},
            onLeave = {},
        )
    }
}

@Preview
@Composable
private fun OpeningHistoryScreenDarkPreview() {
    MiboTheme(darkTheme = true) {
        OpeningHistoryScreen(
            uiState = previewState,
            lockName = "Porta de entrada",
            onLoadMore = {},
            onRetry = {},
            onLeave = {},
        )
    }
}

@Preview
@Composable
private fun OpeningHistoryScreenEmptyPreview() {
    MiboTheme {
        OpeningHistoryScreen(
            uiState = OpeningHistoryUiState(isLoading = false),
            lockName = "Porta de entrada",
            onLoadMore = {},
            onRetry = {},
            onLeave = {},
        )
    }
}
