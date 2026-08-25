package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.auth.AccessTokenStore

internal class SessionTermination(
    private val accessTokenStore: AccessTokenStore,
) {

    suspend operator fun invoke() = accessTokenStore.clear()
}
