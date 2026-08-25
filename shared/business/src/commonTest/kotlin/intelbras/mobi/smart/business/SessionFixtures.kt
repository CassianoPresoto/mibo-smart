package intelbras.mobi.smart.business

import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.domain.auth.model.AccessToken
import kotlin.time.Clock
import kotlin.time.Instant

internal val NOW: Instant = Instant.fromEpochMilliseconds(1_800_000_000_000)

internal class FixedClock(private var instant: Instant = NOW) : Clock {

    override fun now(): Instant = instant

    fun advanceTo(instant: Instant) {
        this.instant = instant
    }
}

internal class InMemoryAccessTokenStore(
    private var storedToken: AccessToken? = null,
) : AccessTokenStore {

    override suspend fun read(): AccessToken? = storedToken

    override suspend fun save(accessToken: AccessToken) {
        storedToken = accessToken
    }

    override suspend fun clear() {
        storedToken = null
    }
}
