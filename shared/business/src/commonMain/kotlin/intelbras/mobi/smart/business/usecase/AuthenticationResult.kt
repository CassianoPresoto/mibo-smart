package intelbras.mobi.smart.business.usecase

sealed interface AuthenticationResult {
    data object Success : AuthenticationResult

    data object MissingToken : AuthenticationResult

    data object InvalidToken : AuthenticationResult

    data object NetworkUnavailable : AuthenticationResult

    data class Error(val cause: Throwable) : AuthenticationResult
}
