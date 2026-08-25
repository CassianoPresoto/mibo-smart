package intelbras.mobi.smart.ui.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import intelbras.mobi.smart.domain.playback.VideoPlayer

@Composable
actual fun rememberVideoPlayer(): VideoPlayer = remember {
    val playback = checkNotNull(IosVideoPlayback.provider?.invoke()) {
        "Nenhum player nativo registrado: defina IosVideoPlayback.provider ao iniciar o app"
    }
    NativeVideoPlayer(playback)
}

@Composable
actual fun VideoPlayerSurface(player: VideoPlayer, modifier: Modifier) {
    val nativePlayer = player as? NativeVideoPlayer ?: return

    UIKitView(
        factory = { nativePlayer.view() },
        modifier = modifier,
    )
}
