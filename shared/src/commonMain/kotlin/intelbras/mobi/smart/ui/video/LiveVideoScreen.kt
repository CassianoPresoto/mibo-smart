package intelbras.mobi.smart.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.domain.playback.VideoPlayer
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_back
import org.jetbrains.compose.resources.stringResource

private const val VIDEO_RATIO = 16f / 9f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveVideoScreen(
    uiState: LiveVideoUiState,
    details: LiveVideoDetails,
    player: VideoPlayer,
    deviceName: String,
    deviceModel: String,
    deviceSerialNumber: String,
    onRetry: () -> Unit,
    onDetailsToggled: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(deviceName) },
                navigationIcon = {
                    TextButton(onClick = onLeave) { Text(stringResource(Res.string.live_video_back)) }
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
            VideoBox(uiState = uiState, player = player)
            Spacer(Modifier.height(16.dp))
            CameraStatusCard(
                uiState = uiState,
                details = details,
                deviceModel = deviceModel,
                deviceSerialNumber = deviceSerialNumber,
                onRetry = onRetry,
                onDetailsToggled = onDetailsToggled,
            )
        }
    }
}

@Composable
private fun VideoBox(uiState: LiveVideoUiState, player: VideoPlayer) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(VIDEO_RATIO)
            .clip(MaterialTheme.shapes.medium)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (uiState == LiveVideoUiState.Playing) {
            VideoPlayerSurface(player = player, modifier = Modifier.fillMaxSize())
        } else if (uiState.isWaitingForPicture()) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

private fun LiveVideoUiState.isWaitingForPicture(): Boolean =
    this == LiveVideoUiState.Connecting ||
        this == LiveVideoUiState.Buffering ||
        this is LiveVideoUiState.Reconnecting
