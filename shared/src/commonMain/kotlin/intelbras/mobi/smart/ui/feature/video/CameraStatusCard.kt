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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_buffering
import mibosmart.shared.generated.resources.live_video_connecting
import mibosmart.shared.generated.resources.live_video_details_consumed
import mibosmart.shared.generated.resources.live_video_details_less
import mibosmart.shared.generated.resources.live_video_details_model
import mibosmart.shared.generated.resources.live_video_details_more
import mibosmart.shared.generated.resources.live_video_details_no_session
import mibosmart.shared.generated.resources.live_video_details_no_usage
import mibosmart.shared.generated.resources.live_video_details_quota
import mibosmart.shared.generated.resources.live_video_details_remaining
import mibosmart.shared.generated.resources.live_video_details_serial
import mibosmart.shared.generated.resources.live_video_details_session
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

private val statusDotSize = 10.dp
private val readingIndicatorSize = 20.dp

@Composable
internal fun CameraStatusCard(
    uiState: LiveVideoUiState,
    details: LiveVideoDetails,
    deviceModel: String,
    deviceSerialNumber: String,
    onRetry: () -> Unit,
    onDetailsToggled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = stringResource(Res.string.live_video_status_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            PlaybackHeadline(uiState)

            uiState.explanation()?.let { explanation ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            uiState.retryLabel()?.let { label ->
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text(label) }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDetailsToggled) {
                Text(stringResource(details.expandLabelResource()))
            }

            if (details.isExpanded) {
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                CameraDetails(details, deviceModel, deviceSerialNumber)
            }
        }
    }
}

@Composable
private fun PlaybackHeadline(uiState: LiveVideoUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(statusDotSize)
                .clip(CircleShape)
                .background(uiState.dotColor()),
        )
        Spacer(Modifier.size(8.dp))
        Text(text = uiState.headline(), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CameraDetails(
    details: LiveVideoDetails,
    deviceModel: String,
    deviceSerialNumber: String,
) {
    DetailRow(stringResource(Res.string.live_video_details_model), deviceModel)
    DetailRow(stringResource(Res.string.live_video_details_serial), deviceSerialNumber)

    if (!details.hasSession) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.live_video_details_no_session),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    DetailRow(stringResource(Res.string.live_video_details_session), details.sessionId)
    DetailRow(
        label = stringResource(Res.string.live_video_details_quota),
        value = stringResource(Res.string.live_video_gigabytes, withOneDecimal(details.quotaGb)),
    )

    when {
        details.isReadingUsage -> {
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(readingIndicatorSize))
        }

        details.usage == null -> {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.live_video_details_no_usage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        else -> UsageRows(details.usage)
    }
}

@Composable
private fun UsageRows(usage: LiveVideoUsage) {
    DetailRow(
        label = stringResource(Res.string.live_video_details_consumed),
        value = stringResource(Res.string.live_video_megabytes, megabytesOf(usage.consumedBytes)),
    )
    DetailRow(
        label = stringResource(Res.string.live_video_details_remaining),
        value = stringResource(
            Res.string.live_video_gigabytes,
            withOneDecimal(usage.remainingQuotaGb),
        ),
    )
    DetailRow(
        label = stringResource(Res.string.live_video_details_session_active),
        value = stringResource(usage.activeLabelResource()),
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LiveVideoUiState.headline(): String = when (this) {
    LiveVideoUiState.Connecting -> stringResource(Res.string.live_video_connecting)
    LiveVideoUiState.Buffering -> stringResource(Res.string.live_video_buffering)
    LiveVideoUiState.Playing -> stringResource(Res.string.live_video_playing)
    is LiveVideoUiState.Reconnecting -> stringResource(Res.string.live_video_reconnecting, attempt)
    LiveVideoUiState.Ended -> stringResource(Res.string.live_video_ended)
    is LiveVideoUiState.Failed -> stringResource(failure.headlineResource())
}

@Composable
private fun LiveVideoUiState.explanation(): String? = when (this) {
    is LiveVideoUiState.Failed -> stringResource(failure.messageResource())
    else -> null
}

@Composable
private fun LiveVideoUiState.retryLabel(): String? = when (this) {
    LiveVideoUiState.Ended -> stringResource(Res.string.live_video_watch_again)
    is LiveVideoUiState.Failed -> stringResource(Res.string.live_video_retry)
    else -> null
}

@Composable
private fun LiveVideoUiState.dotColor(): Color = when (this) {
    LiveVideoUiState.Playing -> MaterialTheme.colorScheme.primary
    is LiveVideoUiState.Failed -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.outline
}

private fun LiveVideoDetails.expandLabelResource() =
    if (isExpanded) Res.string.live_video_details_less else Res.string.live_video_details_more

private fun LiveVideoUsage.activeLabelResource() =
    if (isSessionActive) Res.string.live_video_yes else Res.string.live_video_no

@Preview
@Composable
private fun CameraStatusCardPlayingPreview() {
    PreviewCard(uiState = LiveVideoUiState.Playing, details = LiveVideoDetails())
}

@Preview
@Composable
private fun CameraStatusCardWithDetailsPreview() {
    PreviewCard(
        uiState = LiveVideoUiState.Playing,
        details = LiveVideoDetails(
            isExpanded = true,
            sessionId = "6ecd7198-6a1f-4c7d-9f0e",
            quotaGb = 1.0,
            usage = LiveVideoUsage(
                consumedBytes = 5_242_880L,
                remainingQuotaGb = 0.8,
                isSessionActive = true,
            ),
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
        details = LiveVideoDetails(isExpanded = true),
    )
}

@Composable
private fun PreviewCard(uiState: LiveVideoUiState, details: LiveVideoDetails) {
    MaterialTheme {
        Surface {
            CameraStatusCard(
                uiState = uiState,
                details = details,
                deviceModel = "iM3-C",
                deviceSerialNumber = "KAYK0109140D9",
                onRetry = {},
                onDetailsToggled = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
