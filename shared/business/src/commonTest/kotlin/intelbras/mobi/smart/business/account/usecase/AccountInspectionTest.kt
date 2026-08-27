package intelbras.mobi.smart.business.account.usecase

import intelbras.mobi.smart.business.FixedClock
import intelbras.mobi.smart.business.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.domain.auth.model.AccessToken
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest

class AccountInspectionTest {

    private val clock = FixedClock()
    private val storedToken = AccessToken("Ot_longo_e_secreto_3F9A", NOW + AccessToken.LIFETIME)

    @Test
    fun `reports no session when nothing was ever stored`() = runTest {
        val result = AccountInspection(InMemoryAccessTokenStore(), clock)()

        assertEquals(AccountSummaryResult.SessionMissing, result)
    }

    @Test
    fun `shows only the last characters of the token`() = runTest {
        val summary = summaryOf(InMemoryAccessTokenStore(storedToken))

        assertEquals("3F9A", summary.tokenSuffix)
    }

    @Test
    fun `reports how long the session still has`() = runTest {
        clock.advanceTo(NOW + 18.minutes)

        val summary = summaryOf(InMemoryAccessTokenStore(storedToken))

        assertEquals(102.minutes, summary.expiresIn)
    }

    @Test
    fun `never reports a negative time left`() = runTest {
        clock.advanceTo(NOW + 200.minutes)

        val summary = summaryOf(InMemoryAccessTokenStore(storedToken))

        assertEquals(ZERO, summary.expiresIn)
    }

    private suspend fun summaryOf(store: AccessTokenStore): AccountSummary {
        val result = AccountInspection(store, clock)()

        return assertIs<AccountSummaryResult.Success>(result).account
    }
}
