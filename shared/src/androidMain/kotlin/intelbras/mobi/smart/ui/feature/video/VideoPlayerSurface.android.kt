package intelbras.mobi.smart.ui.feature.video

import android.view.LayoutInflater
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import intelbras.mobi.smart.domain.playback.VideoPlayer
import intelbras.mobi.smart.shared.R

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
    val videoPlayer = player as? ExoPlayerVideoPlayer ?: return

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.mibo_video_surface, null) as PlayerView
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            view
        },
        update = { view ->
            view.player = videoPlayer.exoPlayer
            videoPlayer.attachSurface(view.videoSurfaceView as? TextureView)
        },
        onRelease = { view ->
            view.player = null
            videoPlayer.attachSurface(null)
        },
    )
}
