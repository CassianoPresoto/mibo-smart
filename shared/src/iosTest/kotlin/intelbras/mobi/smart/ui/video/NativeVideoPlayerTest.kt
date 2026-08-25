package intelbras.mobi.smart.ui.video

import dev.mokkery.MockMode
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.domain.playback.model.VideoPlayerEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NativeVideoPlayerTest {

    private val playback = mock<NativeVideoPlayback>(MockMode.autoUnit)
    private val player = NativeVideoPlayer(playback)
    private val source = PlaybackSource.LiveVideo("https://open-casainteligente/stream/1")

    @Test
    fun `hands the stream to the native player and listens to it`() = runTest {
        player.start(source)

        verify { playback.start("https://open-casainteligente/stream/1", player) }
        assertEquals(VideoPlayerEvent.Buffering, player.events.first())
    }

    @Test
    fun `turns what the native player reports into playback events`() = runTest {
        player.start(source)

        player.onPlaying()
        assertEquals(VideoPlayerEvent.Playing, player.events.first())

        player.onEnded()
        assertEquals(VideoPlayerEvent.Ended, player.events.first())
    }

    @Test
    fun `a native failure asks the rule for another attempt`() = runTest {
        player.start(source)

        player.onFailed()

        assertEquals(
            VideoPlayerEvent.Failed(PlaybackFailure.StreamEnded),
            player.events.first(),
        )
    }

    @Test
    fun `stopping reaches the native player`() {
        player.start(source)

        player.stop()

        verify { playback.stop() }
    }
}
