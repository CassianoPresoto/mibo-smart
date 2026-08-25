package intelbras.mobi.smart.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import intelbras.mobi.smart.domain.playback.VideoPlayer

@Composable
expect fun rememberVideoPlayer(): VideoPlayer

@Composable
expect fun VideoPlayerSurface(player: VideoPlayer, modifier: Modifier)
