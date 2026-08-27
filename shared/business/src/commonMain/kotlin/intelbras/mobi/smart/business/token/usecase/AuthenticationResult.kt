package intelbras.mobi.smart.business.token.usecase

import kotlin.time.Instant

sealed interface AuthenticationResult {
    data class Success(val expiresAt: Instant) : AuthenticationResult

    data object MissingToken : AuthenticationResult

    data object InvalidToken : AuthenticationResult

    data object NetworkUnavailable : AuthenticationResult

    data class Error(val cause: Throwable) : AuthenticationResult
}
