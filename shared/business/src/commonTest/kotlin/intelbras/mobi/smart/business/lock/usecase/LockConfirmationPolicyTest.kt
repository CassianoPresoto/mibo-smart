package intelbras.mobi.smart.business.lock.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class LockConfirmationPolicyTest {

    private val policy = LockConfirmationPolicy(attempts = 3, firstWait = 1.seconds)

    @Test
    fun `waits longer before each reading`() {
        assertEquals(1.seconds, policy.waitBefore(1))
        assertEquals(2.seconds, policy.waitBefore(2))
        assertEquals(3.seconds, policy.waitBefore(3))
    }
}
