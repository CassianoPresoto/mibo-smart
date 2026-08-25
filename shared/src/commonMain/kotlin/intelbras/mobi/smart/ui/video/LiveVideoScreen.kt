package intelbras.mobi.smart.ui.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.domain.playback.VideoPlayer
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_back
import mibosmart.shared.generated.resources.live_video_buffering
import mibosmart.shared.generated.resources.live_video_connecting
import mibosmart.shared.generated.resources.live_video_ended
import mibosmart.shared.generated.resources.live_video_reconnecting
import mibosmart.shared.generated.resources.live_video_retry
import mibosmart.shared.generated.resources.live_video_watch_again
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveVideoScreen(
    uiState: LiveVideoUiState,
    player: VideoPlayer,
    deviceName: String,
    onRetry: () -> Unit,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(Color.Black),
        ) {
            if (uiState == LiveVideoUiState.Playing) {
                VideoPlayerSurface(player = player, modifier = Modifier.fillMaxSize())
            }

            when (uiState) {
                LiveVideoUiState.Connecting -> WaitingMessage(Res.string.live_video_connecting)
                LiveVideoUiState.Buffering -> WaitingMessage(Res.string.live_video_buffering)
                LiveVideoUiState.Playing -> Unit
                is LiveVideoUiState.Reconnecting -> ReconnectingMessage(uiState.attempt)
                LiveVideoUiState.Ended -> Interrupted(
                    message = stringResource(Res.string.live_video_ended),
                    action = stringResource(Res.string.live_video_watch_again),
                    onAction = onRetry,
                )

                is LiveVideoUiState.Failed -> Interrupted(
                    message = stringResource(uiState.failure.messageResource()),
                    action = stringResource(Res.string.live_video_retry),
                    onAction = onRetry,
                )
            }
        }
    }
}

@Composable
private fun CenteredOverlay(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(PaddingValues(32.dp)),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun WaitingMessage(message: StringResource) {
    CenteredOverlay {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ReconnectingMessage(attempt: Int) {
    CenteredOverlay {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.live_video_reconnecting, attempt),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Interrupted(message: String, action: String, onAction: () -> Unit) {
    CenteredOverlay {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAction) { Text(action) }
        }
    }
}
