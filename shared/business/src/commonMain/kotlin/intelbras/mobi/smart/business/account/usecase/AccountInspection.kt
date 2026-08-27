package intelbras.mobi.smart.business.account.usecase

import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.domain.auth.model.AccessToken
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

internal class AccountInspection(
    private val accessTokenStore: AccessTokenStore,
    private val clock: Clock,
) {

    suspend operator fun invoke(): AccountSummaryResult {
        val storedToken = accessTokenStore.read() ?: return AccountSummaryResult.SessionMissing

        return AccountSummaryResult.Success(
            AccountSummary(
                tokenSuffix = storedToken.suffix(),
                expiresIn = storedToken.remainingLifetime(),
            ),
        )
    }

    private fun AccessToken.suffix(): String = value.takeLast(SUFFIX_LENGTH)

    private fun AccessToken.remainingLifetime(): Duration =
        (expiresAt - clock.now()).coerceAtLeast(ZERO)

    private companion object {
        const val SUFFIX_LENGTH = 4
    }
}
