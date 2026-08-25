package intelbras.mobi.smart.ui.feature.video

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import intelbras.mobi.smart.domain.playback.VideoPlayer

@Composable
actual fun rememberVideoPlayer(): VideoPlayer {
    val context = LocalContext.current
    val player = remember(context) { ExoPlayerVideoPlayer(context) }

    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}

@Composable
actual fun VideoPlayerSurface(player: VideoPlayer, modifier: Modifier) {
    val exoPlayer = (player as? ExoPlayerVideoPlayer)?.exoPlayer ?: return

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        update = { view -> view.player = exoPlayer },
        onRelease = { view -> view.player = null },
    )
}
