package intelbras.mobi.smart.business.session

import intelbras.mobi.smart.domain.auth.AccessTokenStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class InMemoryAccessTokenStore : AccessTokenStore {

    private val guard = Mutex()
    private var accessToken: String? = null

    override suspend fun currentAccessToken(): String? = guard.withLock { accessToken }

    override suspend fun save(accessToken: String) = guard.withLock {
        this.accessToken = accessToken
    }

    override suspend fun clear() = guard.withLock {
        accessToken = null
    }
}
