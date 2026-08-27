package intelbras.mobi.smart.business.session.usecase

import intelbras.mobi.smart.business.FixedClock
import intelbras.mobi.smart.business.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.domain.auth.model.AccessToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class SessionInspectionTest {

    private val clock = FixedClock()

    @Test
    fun `reports no session when nothing was ever stored`() = runTest {
        val result = SessionInspection(InMemoryAccessTokenStore(), clock)()

        assertEquals(SessionStatus.None, result)
    }

    @Test
    fun `reports an active session while the token is inside its validity`() = runTest {
        val expiresAt = NOW + AccessToken.LIFETIME
        val store = InMemoryAccessTokenStore(AccessToken("Ot_token", expiresAt))
        clock.advanceTo(NOW + 119.minutes)

        val result = SessionInspection(store, clock)()

        assertEquals(SessionStatus.Active(expiresAt), result)
    }

    @Test
    fun `reports the session as expired once two hours have passed`() = runTest {
        val store = InMemoryAccessTokenStore(AccessToken("Ot_token", NOW + AccessToken.LIFETIME))
        clock.advanceTo(NOW + 121.minutes)

        val result = SessionInspection(store, clock)()

        assertEquals(SessionStatus.Expired, result)
    }

    @Test
    fun `discards the token it found expired`() = runTest {
        val store = InMemoryAccessTokenStore(AccessToken("Ot_token", NOW + AccessToken.LIFETIME))
        clock.advanceTo(NOW + 121.minutes)

        SessionInspection(store, clock)()

        assertNull(store.read())
    }
}
