package intelbras.mobi.smart.ui.token

import kotlin.time.Instant

sealed interface TokenEntryUiState {
    data object CheckingStoredSession : TokenEntryUiState

    data class AwaitingToken(
        val token: String = "",
        val isSubmitting: Boolean = false,
        val failure: TokenEntryFailure? = null,
    ) : TokenEntryUiState {
        val canSubmit: Boolean get() = token.isNotBlank() && !isSubmitting
    }

    data class Authenticated(val expiresAt: Instant) : TokenEntryUiState
}

sealed interface TokenEntryFailure {
    data object EmptyToken : TokenEntryFailure

    data object InvalidToken : TokenEntryFailure

    data object ExpiredSession : TokenEntryFailure

    data object NetworkUnavailable : TokenEntryFailure

    data object Unexpected : TokenEntryFailure
}
