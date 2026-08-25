package intelbras.mobi.smart.business.session

import intelbras.mobi.smart.domain.auth.AccessTokenProvider
import intelbras.mobi.smart.domain.auth.AccessTokenStore
import kotlin.time.Clock

internal class StoredAccessTokenProvider(
    private val accessTokenStore: AccessTokenStore,
    private val clock: Clock,
) : AccessTokenProvider {

    override suspend fun currentAccessToken(): String? =
        accessTokenStore.read()
            ?.takeIf { storedToken -> storedToken.isValidAt(clock.now()) }
            ?.value
}
