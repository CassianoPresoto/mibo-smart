package intelbras.mobi.smart.business.video.usecase

import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class PlaybackRetryPolicyTest {

    private val policy = PlaybackRetryPolicy(maxAttempts = 3, waitBetweenAttempts = 2.seconds)

    @Test
    fun `tries again when the stream or the network dropped`() {
        assertTrue(policy.retriesAfter(PlaybackFailure.Network))
        assertTrue(policy.retriesAfter(PlaybackFailure.StreamEnded))
    }

    @Test
    fun `gives up when the failure is in the playback itself`() {
        assertFalse(policy.retriesAfter(PlaybackFailure.Playback))
    }

    @Test
    fun `stops offering attempts once the limit is reached`() {
        assertTrue(policy.hasAnotherAttemptAfter(2))
        assertFalse(policy.hasAnotherAttemptAfter(3))
    }

    @Test
    fun `waits longer before each attempt`() {
        assertEquals(2.seconds, policy.waitBefore(1))
        assertEquals(4.seconds, policy.waitBefore(2))
        assertEquals(6.seconds, policy.waitBefore(3))
    }

    @Test
    fun `only the first attempt reuses the open connection`() {
        assertFalse(policy.reopensConnectionOn(1))
        assertTrue(policy.reopensConnectionOn(2))
    }
}
