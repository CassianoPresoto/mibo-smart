package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.auth.AccessTokenStore
import kotlin.time.Clock

internal class SessionInspection(
    private val accessTokenStore: AccessTokenStore,
    private val clock: Clock,
) {

    suspend operator fun invoke(): SessionStatus {
        val storedToken = accessTokenStore.read() ?: return SessionStatus.None
        if (storedToken.isValidAt(clock.now())) return SessionStatus.Active(storedToken.expiresAt)

        accessTokenStore.clear()
        return SessionStatus.Expired
    }
}
