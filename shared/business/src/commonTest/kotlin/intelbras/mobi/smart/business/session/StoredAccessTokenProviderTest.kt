package intelbras.mobi.smart.business.session

import intelbras.mobi.smart.business.FixedClock
import intelbras.mobi.smart.business.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.domain.auth.model.AccessToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class StoredAccessTokenProviderTest {

    private val clock = FixedClock()

    @Test
    fun `hands over the stored token while it is still valid`() = runTest {
        val store = InMemoryAccessTokenStore(AccessToken("Ot_token", NOW + AccessToken.LIFETIME))

        assertEquals("Ot_token", StoredAccessTokenProvider(store, clock).currentAccessToken())
    }

    @Test
    fun `hands over nothing once the token expired`() = runTest {
        val store = InMemoryAccessTokenStore(AccessToken("Ot_token", NOW + AccessToken.LIFETIME))
        clock.advanceTo(NOW + 121.minutes)

        assertNull(StoredAccessTokenProvider(store, clock).currentAccessToken())
    }

    @Test
    fun `hands over nothing when there is no session`() = runTest {
        assertNull(StoredAccessTokenProvider(InMemoryAccessTokenStore(), clock).currentAccessToken())
    }
}
