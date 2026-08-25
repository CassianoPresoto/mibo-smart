package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.LiveVideoPlayback
import intelbras.mobi.smart.business.usecase.VideoPlaybackState
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.playback.VideoPlayer
import kotlinx.coroutines.flow.Flow

internal class VideoPlaybackImpl(
    private val liveVideoPlayback: LiveVideoPlayback,
) : VideoPlayback {

    override fun play(device: DeviceReference, player: VideoPlayer): Flow<VideoPlaybackState> =
        liveVideoPlayback(device, player)
}
