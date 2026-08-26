package intelbras.mobi.smart.ui.feature.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.ui.component.MiboCard
import intelbras.mobi.smart.ui.component.MiboDetailRow
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_buffering
import mibosmart.shared.generated.resources.live_video_connecting
import mibosmart.shared.generated.resources.live_video_details_consumed
import mibosmart.shared.generated.resources.live_video_details_model
import mibosmart.shared.generated.resources.live_video_details_no_session
import mibosmart.shared.generated.resources.live_video_details_no_usage
import mibosmart.shared.generated.resources.live_video_details_quota
import mibosmart.shared.generated.resources.live_video_details_remaining
import mibosmart.shared.generated.resources.live_video_details_serial
import mibosmart.shared.generated.resources.live_video_details_session_active
import mibosmart.shared.generated.resources.live_video_ended
import mibosmart.shared.generated.resources.live_video_gigabytes
import mibosmart.shared.generated.resources.live_video_megabytes
import mibosmart.shared.generated.resources.live_video_no
import mibosmart.shared.generated.resources.live_video_playing
import mibosmart.shared.generated.resources.live_video_reconnecting
import mibosmart.shared.generated.resources.live_video_retry
import mibosmart.shared.generated.resources.live_video_status_title
import mibosmart.shared.generated.resources.live_video_watch_again
import mibosmart.shared.generated.resources.live_video_yes
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CameraStatusCard(
    uiState: LiveVideoUiState,
    details: LiveVideoDetails,
    deviceModel: String,
    deviceSerialNumber: String,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors

    MiboCard(modifier = modifier) {
        StatusHeader(uiState)

        if (details.hasSession) {
            Spacer(Modifier.height(14.dp))
            UsageOverview(details)
            details.usage?.let { usage ->
                MiboDetailRow(
                    label = stringResource(Res.string.live_video_details_session_active),
                    value = stringResource(usage.activeLabelResource()),
                )
                MiboDetailRow(
                    label = stringResource(Res.string.live_video_details_remaining),
                    value = stringResource(Res.string.live_video_gigabytes, withOneDecimal(usage.remainingQuotaGb)),
                    monospace = true,
                )
            }
        } else {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = colors.outline, thickness = MiboSmartSize.hairline)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.live_video_details_no_session),
                style = MiboTheme.typography.body,
                color = colors.muted,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        MiboDetailRow(stringResource(Res.string.live_video_details_model), deviceModel)
        MiboDetailRow(
            label = stringResource(Res.string.live_video_details_serial),
            value = deviceSerialNumber,
            monospace = true,
        )
    }
}

@Composable
private fun StatusHeader(uiState: LiveVideoUiState) {
    val colors = MiboTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.live_video_status_title),
            style = MiboTheme.typography.subtitle,
            color = colors.text,
        )
        Text(
            text = uiState.headline(),
            style = MiboTheme.typography.subtitle,
            color = uiState.statusColor(),
        )
    }
}

@Composable
private fun UsageOverview(details: LiveVideoDetails) {
    val colors = MiboTheme.colors

    if (details.isReadingUsage) {
        Box(Modifier.padding(bottom = 12.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
        return
    }

    val usage = details.usage
    if (usage == null) {
        Text(
            text = stringResource(Res.string.live_video_details_no_usage),
            style = MiboTheme.typography.body,
            color = colors.muted,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        return
    }

    UsageProgressBar(fraction = consumedFractionOf(usage, details.quotaGb))
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.live_video_details_consumed).lowercase() + " " +
                stringResource(Res.string.live_video_megabytes, megabytesOf(usage.consumedBytes)),
            style = MiboTheme.typography.monoSmall,
            color = colors.muted,
        )
        Text(
            text = stringResource(Res.string.live_video_details_quota).lowercase() + " " +
                stringResource(Res.string.live_video_gigabytes, withOneDecimal(details.quotaGb)),
            style = MiboTheme.typography.monoSmall,
            color = colors.muted,
        )
    }
}

@Composable
private fun UsageProgressBar(fraction: Float) {
    val colors = MiboTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.codeSurface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.primary),
        )
    }
}

@Composable
internal fun LiveVideoUiState.headline(): String = when (this) {
    LiveVideoUiState.Connecting -> stringResource(Res.string.live_video_connecting)
    LiveVideoUiState.Buffering -> stringResource(Res.string.live_video_buffering)
    LiveVideoUiState.Playing -> stringResource(Res.string.live_video_playing)
    is LiveVideoUiState.Reconnecting -> stringResource(Res.string.live_video_reconnecting, attempt)
    LiveVideoUiState.Ended -> stringResource(Res.string.live_video_ended)
    is LiveVideoUiState.Failed -> stringResource(failure.headlineResource())
}

@Composable
internal fun LiveVideoUiState.explanation(): String? = when (this) {
    is LiveVideoUiState.Failed -> stringResource(failure.messageResource())
    else -> null
}

@Composable
internal fun LiveVideoUiState.retryLabel(): String? = when (this) {
    LiveVideoUiState.Ended -> stringResource(Res.string.live_video_watch_again)
    is LiveVideoUiState.Failed -> stringResource(Res.string.live_video_retry)
    else -> null
}

@Composable
internal fun LiveVideoUiState.statusColor(): Color = when (this) {
    LiveVideoUiState.Playing -> MiboTheme.colors.primary
    is LiveVideoUiState.Failed -> MiboTheme.colors.danger
    else -> MiboTheme.colors.muted
}

private fun LiveVideoUsage.activeLabelResource() =
    if (isSessionActive) Res.string.live_video_yes else Res.string.live_video_no

@Preview
@Composable
private fun CameraStatusCardPlayingPreview() {
    PreviewCard(
        uiState = LiveVideoUiState.Playing,
        details = LiveVideoDetails(
            sessionId = "6ecd7198-6a1f-4c7d-9f0e",
            quotaGb = 1.0,
            usage = LiveVideoUsage(
                consumedBytes = 148_897_792L,
                remainingQuotaGb = 0.86,
                isSessionActive = true,
            ),
        ),
    )
}

@Preview
@Composable
private fun CameraStatusCardLoadingUsagePreview() {
    PreviewCard(
        uiState = LiveVideoUiState.Playing,
        details = LiveVideoDetails(
            isReadingUsage = true,
            sessionId = "6ecd7198-6a1f-4c7d-9f0e",
            quotaGb = 1.0,
        ),
    )
}

@Preview
@Composable
private fun CameraStatusCardReconnectingPreview() {
    PreviewCard(uiState = LiveVideoUiState.Reconnecting(attempt = 2), details = LiveVideoDetails())
}

@Preview
@Composable
private fun CameraStatusCardFailedPreview() {
    PreviewCard(
        uiState = LiveVideoUiState.Failed(LiveVideoFailure.DeviceOffline),
        details = LiveVideoDetails(),
    )
}

@Composable
private fun PreviewCard(uiState: LiveVideoUiState, details: LiveVideoDetails) {
    MiboTheme {
        Surface {
            CameraStatusCard(
                uiState = uiState,
                details = details,
                deviceModel = "iM3-C",
                deviceSerialNumber = "KAYK0109140D9",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
