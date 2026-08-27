package intelbras.mobi.smart.ui.feature.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.token.usecase.AuthenticationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TokenEntryViewModel(
    private val smartHomeSession: SmartHomeSession,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(TokenEntryUiState())
    val uiState: StateFlow<TokenEntryUiState> = mutableUiState.asStateFlow()

    fun onTokenChanged(token: String) {
        mutableUiState.update { state -> state.copy(token = token, failure = null) }
    }

    fun onSubmit() {
        val state = mutableUiState.value
        if (state.isSubmitting) return

        mutableUiState.value = state.copy(isSubmitting = true, failure = null)
        viewModelScope.launch {
            mutableUiState.update { submitting ->
                smartHomeSession.authenticate(submitting.token).applyTo(submitting)
            }
        }
    }

    private fun AuthenticationResult.applyTo(state: TokenEntryUiState): TokenEntryUiState =
        when (this) {
            is AuthenticationResult.Success -> state.copy(isSubmitting = false, isAuthenticated = true)
            AuthenticationResult.MissingToken -> state.failing(TokenEntryFailure.EmptyToken)
            AuthenticationResult.InvalidToken -> state.failing(TokenEntryFailure.InvalidToken)
            AuthenticationResult.NetworkUnavailable ->
                state.failing(TokenEntryFailure.NetworkUnavailable)

            is AuthenticationResult.Error -> state.failing(TokenEntryFailure.Unexpected)
        }

    private fun TokenEntryUiState.failing(failure: TokenEntryFailure) =
        copy(isSubmitting = false, failure = failure)
}
