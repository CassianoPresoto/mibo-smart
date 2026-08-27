package intelbras.mobi.smart.ui.feature.video

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import intelbras.mobi.smart.ui.component.MiboBackButton
import intelbras.mobi.smart.ui.component.MiboCompactButton
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureUiModel
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureUiState
import intelbras.mobi.smart.ui.feature.video.capture.CaptureNotice
import intelbras.mobi.smart.ui.feature.video.capture.CaptureRecordingUiState
import intelbras.mobi.smart.ui.feature.video.capture.messageResource
import intelbras.mobi.smart.ui.feature.video.component.CaptureControls
import intelbras.mobi.smart.ui.feature.video.component.CaptureLibraryCard
import intelbras.mobi.smart.ui.feature.video.component.RecordingBadge
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_back
import mibosmart.shared.generated.resources.live_video_buffering
import mibosmart.shared.generated.resources.live_video_connecting
import mibosmart.shared.generated.resources.live_video_live_badge
import mibosmart.shared.generated.resources.live_video_reconnecting
import org.jetbrains.compose.resources.stringResource

private const val VIDEO_RATIO = 16f / 9f
private const val CLOCK_TICK_MS = 1_000L
private const val NOTICE_DURATION_MS = 2_600L
private const val FLASH_ALPHA = 0.75f
private const val NO_FLASH = 0f
private const val FLASH_DURATION_MS = 160

@Composable
internal fun LiveVideoScreen(
    uiState: LiveVideoUiState,
    details: LiveVideoDetails,
    captures: CameraCaptureUiState,
    player: VideoPlayer,
    deviceName: String,
    deviceModel: String,
    deviceSerialNumber: String,
    loadPreview: suspend (String) -> ByteArray?,
    onRetry: () -> Unit,
    onTakePhoto: () -> Unit,
    onToggleRecording: () -> Unit,
    onNoticeShown: () -> Unit,
    onCaptureClick: (CameraCaptureUiModel) -> Unit,
    onSeeAllCaptures: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
    ) {
        LiveVideoHeader(
            deviceName = deviceName,
            deviceModel = deviceModel,
            onLeave = onLeave,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MiboSmartSize.listPadding)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
        ) {
            VideoBox(
                uiState = uiState,
                captures = captures,
                player = player,
                onRetry = onRetry,
                onTakePhoto = onTakePhoto,
                onToggleRecording = onToggleRecording,
                onNoticeShown = onNoticeShown,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(VIDEO_RATIO),
            )
            Spacer(Modifier.height(16.dp))
            CameraStatusCard(
                uiState = uiState,
                details = details,
                deviceModel = deviceModel,
                deviceSerialNumber = deviceSerialNumber,
            )
            Spacer(Modifier.height(MiboSmartSpacing.md))
            CaptureLibraryCard(
                captures = captures.captures,
                loadPreview = loadPreview,
                onCaptureClick = onCaptureClick,
                onSeeAll = onSeeAllCaptures,
            )
        }
    }
}

@Composable
private fun LiveVideoHeader(
    deviceName: String,
    deviceModel: String,
    onLeave: () -> Unit,
) {
    val colors = MiboTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MiboSmartSize.listPadding,
                end = MiboSmartSize.listPadding,
                top = 14.dp,
                bottom = 10.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiboBackButton(
            description = stringResource(Res.string.live_video_back),
            onClick = onLeave,
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = deviceName,
                style = MiboTheme.typography.subtitle.copy(fontSize = 18.sp),
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (deviceModel.isNotBlank()) {
                Text(
                    text = deviceModel,
                    style = MiboTheme.typography.caption.copy(fontSize = 11.5.sp),
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun VideoBox(
    uiState: LiveVideoUiState,
    captures: CameraCaptureUiState,
    player: VideoPlayer,
    onRetry: () -> Unit,
    onTakePhoto: () -> Unit,
    onToggleRecording: () -> Unit,
    onNoticeShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    Box(
        modifier = modifier
            .clip(MiboSmartShapes.card)
            .background(colors.videoBackdrop),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState == LiveVideoUiState.Playing -> {
                VideoPlayerSurface(player = player, modifier = Modifier.fillMaxSize())
                Row(
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiveBadge()
                    val recording = captures.recording
                    if (recording is CaptureRecordingUiState.Recording) {
                        RecordingBadge(elapsedSeconds = recording.elapsedSeconds)
                    }
                }
                VideoClock(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                CaptureControls(
                    state = captures,
                    onTakePhoto = onTakePhoto,
                    onToggleRecording = onToggleRecording,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                )
                CaptureFlash(active = captures.isTakingPhoto)
            }

            uiState.isWaitingForPicture() -> {
                WaitingIndicator(uiState)
            }

            uiState.retryLabel() != null -> {
                VideoRetryOverlay(uiState = uiState, onRetry = onRetry)
            }
        }

        captures.notice?.let { notice ->
            CaptureNoticeBanner(
                notice = notice,
                onShown = onNoticeShown,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun CaptureFlash(active: Boolean) {
    val alpha by animateFloatAsState(
        targetValue = if (active) FLASH_ALPHA else NO_FLASH,
        animationSpec = tween(durationMillis = FLASH_DURATION_MS),
        label = "captureFlash",
    )
    if (alpha == NO_FLASH) return

    Box(
        Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(MiboTheme.colors.onVideo),
    )
}

@Composable
private fun CaptureNoticeBanner(
    notice: CaptureNotice,
    onShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors

    LaunchedEffect(notice) {
        delay(NOTICE_DURATION_MS)
        onShown()
    }

    Text(
        text = stringResource(notice.messageResource()),
        style = MiboTheme.typography.caption.copy(fontSize = 12.sp),
        color = colors.onVideo,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(MiboSmartShapes.pill)
            .background(colors.videoScrim)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

@Composable
private fun WaitingIndicator(uiState: LiveVideoUiState) {
    val colors = MiboTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = colors.onVideo, modifier = Modifier.size(30.dp))
        Text(
            text = uiState.waitingLabel(),
            style = MiboTheme.typography.caption.copy(fontSize = 12.5.sp),
            color = colors.onVideo.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun VideoRetryOverlay(uiState: LiveVideoUiState, onRetry: () -> Unit) {
    val colors = MiboTheme.colors
    Column(
        modifier = Modifier.padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = uiState.headline(),
            style = MiboTheme.typography.subtitle.copy(fontSize = 15.sp),
            color = colors.onVideo,
            textAlign = TextAlign.Center,
        )
        uiState.explanation()?.let { explanation ->
            Text(
                text = explanation,
                style = MiboTheme.typography.caption.copy(fontSize = 12.sp, lineHeight = 18.sp),
                color = colors.onVideo.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(4.dp))
        MiboCompactButton(text = uiState.retryLabel().orEmpty(), onClick = onRetry)
    }
}

@Composable
private fun LiveBadge(modifier: Modifier = Modifier) {
    val colors = MiboTheme.colors
    val transition = rememberInfiniteTransition(label = "liveDot")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveDotAlpha",
    )
    Row(
        modifier = modifier
            .clip(MiboSmartShapes.pill)
            .background(colors.videoScrim)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(colors.live.copy(alpha = alpha)),
        )
        Text(
            text = stringResource(Res.string.live_video_live_badge),
            style = MiboTheme.typography.label.copy(fontSize = 11.sp, letterSpacing = 0.6.sp),
            color = colors.onVideo,
        )
    }
}

@Composable
private fun VideoClock(modifier: Modifier = Modifier) {
    val colors = MiboTheme.colors
    var now by rememberSaveable { mutableStateOf(currentTimestamp()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = currentTimestamp()
            delay(CLOCK_TICK_MS)
        }
    }

    Text(
        text = now,
        style = MiboTheme.typography.mono.copy(fontSize = 10.5.sp, letterSpacing = 0.sp),
        color = colors.onVideo.copy(alpha = 0.75f),
        modifier = modifier,
    )
}

@OptIn(ExperimentalTime::class)
private fun currentTimestamp(): String {
    val now: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val day = now.day.twoDigits()
    val month = (now.month.ordinal + 1).twoDigits()
    val hour = now.hour.twoDigits()
    val minute = now.minute.twoDigits()
    val second = now.second.twoDigits()
    return "$day/$month · $hour:$minute:$second"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private fun LiveVideoUiState.isWaitingForPicture(): Boolean =
    this == LiveVideoUiState.Connecting ||
        this == LiveVideoUiState.Buffering ||
        this is LiveVideoUiState.Reconnecting

@Composable
private fun LiveVideoUiState.waitingLabel(): String = when (this) {
    LiveVideoUiState.Connecting -> stringResource(Res.string.live_video_connecting)
    LiveVideoUiState.Buffering -> stringResource(Res.string.live_video_buffering)
    is LiveVideoUiState.Reconnecting -> stringResource(Res.string.live_video_reconnecting, attempt)
    else -> ""
}

private object PreviewVideoPlayer : VideoPlayer {
    override val events: Flow<VideoPlayerEvent> = emptyFlow()
    override fun start(source: PlaybackSource) = Unit
    override fun stop() = Unit
}

@Preview
@Composable
private fun LiveVideoScreenPlayingPreview() {
    PreviewScreen(
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
        captures = CameraCaptureUiState(canTakePhoto = true, canRecord = true),
    )
}

@Preview
@Composable
private fun LiveVideoScreenRecordingPreview() {
    PreviewScreen(
        uiState = LiveVideoUiState.Playing,
        details = LiveVideoDetails(sessionId = "6ecd7198", quotaGb = 1.0),
        captures = CameraCaptureUiState(
            canTakePhoto = true,
            canRecord = true,
            recording = CaptureRecordingUiState.Recording(elapsedSeconds = 12),
        ),
    )
}

@Preview
@Composable
private fun LiveVideoScreenConnectingPreview() {
    PreviewScreen(uiState = LiveVideoUiState.Connecting, details = LiveVideoDetails())
}

@Preview
@Composable
private fun LiveVideoScreenFailedPreview() {
    PreviewScreen(
        uiState = LiveVideoUiState.Failed(LiveVideoFailure.DeviceOffline),
        details = LiveVideoDetails(),
    )
}

@Composable
private fun PreviewScreen(
    uiState: LiveVideoUiState,
    details: LiveVideoDetails,
    captures: CameraCaptureUiState = CameraCaptureUiState(),
) {
    MiboTheme {
        LiveVideoScreen(
            uiState = uiState,
            details = details,
            captures = captures,
            player = PreviewVideoPlayer,
            deviceName = "Sala",
            deviceModel = "Mibo Cloud iM5 S",
            deviceSerialNumber = "4H0A2C1D9B",
            loadPreview = { null },
            onRetry = {},
            onTakePhoto = {},
            onToggleRecording = {},
            onNoticeShown = {},
            onCaptureClick = {},
            onSeeAllCaptures = {},
            onLeave = {},
        )
    }
}
