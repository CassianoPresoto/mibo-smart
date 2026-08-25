package intelbras.mobi.smart.ui.video

import androidx.media3.common.PlaybackException
import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaybackFailureMappingTest {

    @Test
    fun `a broken connection is a network failure`() {
        assertEquals(
            PlaybackFailure.Network,
            playbackFailureOf(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED),
        )
        assertEquals(
            PlaybackFailure.Network,
            playbackFailureOf(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT),
        )
        assertEquals(
            PlaybackFailure.Network,
            playbackFailureOf(PlaybackException.ERROR_CODE_TIMEOUT),
        )
    }

    @Test
    fun `a stream the platform no longer serves is a dead session`() {
        assertEquals(
            PlaybackFailure.StreamEnded,
            playbackFailureOf(PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS),
        )
        assertEquals(
            PlaybackFailure.StreamEnded,
            playbackFailureOf(PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND),
        )
        assertEquals(
            PlaybackFailure.StreamEnded,
            playbackFailureOf(PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW),
        )
    }

    @Test
    fun `a decoding problem is not worth another attempt`() {
        assertEquals(
            PlaybackFailure.Playback,
            playbackFailureOf(PlaybackException.ERROR_CODE_DECODING_FAILED),
        )
        assertEquals(
            PlaybackFailure.Playback,
            playbackFailureOf(PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED),
        )
        assertEquals(
            PlaybackFailure.Playback,
            playbackFailureOf(PlaybackException.ERROR_CODE_UNSPECIFIED),
        )
    }
}
