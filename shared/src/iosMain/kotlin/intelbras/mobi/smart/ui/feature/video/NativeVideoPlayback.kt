package intelbras.mobi.smart.ui.feature.video

import platform.UIKit.UIView

interface NativeVideoPlayback {
    fun view(): UIView

    fun start(url: String, listener: NativeVideoPlaybackListener)

    fun stop()

    fun takeSnapshot(path: String)

    fun startRecording(directoryPath: String)

    fun stopRecording()
}

interface NativeVideoPlaybackListener {
    fun onBuffering()

    fun onPlaying()

    fun onEnded()

    fun onFailed()

    fun onSnapshotTaken(path: String?)

    fun onRecordingStarted()

    fun onRecordingStopped(path: String?)
}

object IosVideoPlayback {
    var provider: (() -> NativeVideoPlayback)? = null
}
