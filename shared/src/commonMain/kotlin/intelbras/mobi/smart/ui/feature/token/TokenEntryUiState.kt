package intelbras.mobi.smart.ui.feature.token

data class TokenEntryUiState(
    val token: String = "",
    val isSubmitting: Boolean = false,
    val failure: TokenEntryFailure? = null,
    val isAuthenticated: Boolean = false,
) {
    val canSubmit: Boolean get() = token.isNotBlank() && !isSubmitting
}

sealed interface TokenEntryFailure {
    data object EmptyToken : TokenEntryFailure

    data object InvalidToken : TokenEntryFailure

    data object ExpiredSession : TokenEntryFailure

    data object NetworkUnavailable : TokenEntryFailure

    data object Unexpected : TokenEntryFailure
}
