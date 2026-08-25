package intelbras.mobi.smart.business.usecase

import kotlin.time.Instant

sealed interface SessionStatus {
    data class Active(val expiresAt: Instant) : SessionStatus

    data object Expired : SessionStatus

    data object None : SessionStatus
}
