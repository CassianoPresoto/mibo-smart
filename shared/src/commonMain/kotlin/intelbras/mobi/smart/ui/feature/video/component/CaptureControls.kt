package intelbras.mobi.smart.ui.feature.video.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.component.MiboVideoControlButton
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureUiState
import intelbras.mobi.smart.ui.feature.video.capture.CaptureRecordingUiState
import intelbras.mobi.smart.ui.feature.video.capture.elapsedLabelOf
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_recording_badge
import mibosmart.shared.generated.resources.live_video_start_recording
import mibosmart.shared.generated.resources.live_video_stop_recording
import mibosmart.shared.generated.resources.live_video_take_photo
import org.jetbrains.compose.resources.stringResource

private val savingIndicatorSize = 18.dp
private val recordingDotSize = 7.dp

@Composable
internal fun CaptureControls(
    state: CameraCaptureUiState,
    onTakePhoto: () -> Unit,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.canRecord) RecordingButton(state = state, onToggleRecording = onToggleRecording)
        if (state.canTakePhoto) {
            MiboVideoControlButton(
                icon = Icons.Filled.PhotoCamera,
                description = stringResource(Res.string.live_video_take_photo),
                onClick = onTakePhoto,
                enabled = !state.isTakingPhoto,
            )
        }
    }
}

@Composable
private fun RecordingButton(state: CameraCaptureUiState, onToggleRecording: () -> Unit) {
    val colors = MiboTheme.colors

    if (state.recording is CaptureRecordingUiState.Saving) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.videoControl),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = colors.onVideo,
                strokeWidth = 2.dp,
                modifier = Modifier.size(savingIndicatorSize),
            )
        }
        return
    }

    MiboVideoControlButton(
        icon = if (state.isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
        description = stringResource(
            if (state.isRecording) Res.string.live_video_stop_recording
            else Res.string.live_video_start_recording,
        ),
        onClick = onToggleRecording,
        tint = if (state.isRecording) colors.onVideo else colors.live,
    )
}

@Composable
internal fun RecordingBadge(elapsedSeconds: Int, modifier: Modifier = Modifier) {
    val colors = MiboTheme.colors
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
                .size(recordingDotSize)
                .clip(CircleShape)
                .background(colors.live),
        )
        Text(
            text = stringResource(Res.string.live_video_recording_badge, elapsedLabelOf(elapsedSeconds)),
            style = MiboTheme.typography.mono.copy(fontSize = 11.sp, letterSpacing = 0.sp),
            color = colors.onVideo,
        )
    }
}
