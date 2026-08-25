package intelbras.mobi.smart.ui.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.SmartHomeSession
import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TokenEntryViewModel(
    private val smartHomeSession: SmartHomeSession,
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow<TokenEntryUiState>(TokenEntryUiState.CheckingStoredSession)
    val uiState: StateFlow<TokenEntryUiState> = mutableUiState.asStateFlow()

    init {
        restoreStoredSession()
    }

    fun onTokenChanged(token: String) {
        mutableUiState.update { current ->
            when (current) {
                is TokenEntryUiState.AwaitingToken -> current.copy(token = token, failure = null)
                else -> current
            }
        }
    }

    fun onSubmit() {
        val awaiting = mutableUiState.value as? TokenEntryUiState.AwaitingToken ?: return
        if (awaiting.isSubmitting) return

        mutableUiState.value = awaiting.copy(isSubmitting = true, failure = null)
        viewModelScope.launch {
            mutableUiState.value = smartHomeSession.authenticate(awaiting.token).toUiState(awaiting)
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            smartHomeSession.signOut()
            mutableUiState.value = TokenEntryUiState.AwaitingToken()
        }
    }

    private fun restoreStoredSession() {
        viewModelScope.launch {
            mutableUiState.value = when (val status = smartHomeSession.currentStatus()) {
                is SessionStatus.Active -> TokenEntryUiState.Authenticated(status.expiresAt)
                SessionStatus.Expired ->
                    TokenEntryUiState.AwaitingToken(failure = TokenEntryFailure.ExpiredSession)

                SessionStatus.None -> TokenEntryUiState.AwaitingToken()
            }
        }
    }

    private fun AuthenticationResult.toUiState(
        awaiting: TokenEntryUiState.AwaitingToken,
    ): TokenEntryUiState = when (this) {
        is AuthenticationResult.Success -> TokenEntryUiState.Authenticated(expiresAt)
        AuthenticationResult.MissingToken -> awaiting.failing(TokenEntryFailure.EmptyToken)
        AuthenticationResult.InvalidToken -> awaiting.failing(TokenEntryFailure.InvalidToken)
        AuthenticationResult.NetworkUnavailable ->
            awaiting.failing(TokenEntryFailure.NetworkUnavailable)

        is AuthenticationResult.Error -> awaiting.failing(TokenEntryFailure.Unexpected)
    }

    private fun TokenEntryUiState.AwaitingToken.failing(failure: TokenEntryFailure) =
        copy(isSubmitting = false, failure = failure)
}
