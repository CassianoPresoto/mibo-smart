package intelbras.mobi.smart.ui.feature.video

import platform.UIKit.UIView

interface NativeVideoPlayback {
    fun view(): UIView

    fun start(url: String, listener: NativeVideoPlaybackListener)

    fun stop()
}

interface NativeVideoPlaybackListener {
    fun onBuffering()

    fun onPlaying()

    fun onEnded()

    fun onFailed()
}

object IosVideoPlayback {
    var provider: (() -> NativeVideoPlayback)? = null
}
