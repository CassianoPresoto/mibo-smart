package intelbras.mobi.smart.ui.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.theme.ThemeSettings
import intelbras.mobi.smart.business.account.UserAccount
import intelbras.mobi.smart.business.account.usecase.AccountSummaryResult
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val userAccount: UserAccount,
    private val themeSettings: ThemeSettings,
    private val smartHomeSession: SmartHomeSession,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = mutableUiState.asStateFlow()

    init {
        loadSummary()
        observeTheme()
    }

    fun onDarkThemeToggled(dark: Boolean) {
        val chosen = if (dark) ThemeMode.Dark else ThemeMode.Light
        viewModelScope.launch { themeSettings.choose(chosen) }
    }

    fun onSignOut() {
        if (mutableUiState.value.isSigningOut) return

        mutableUiState.value = mutableUiState.value.copy(isSigningOut = true)
        viewModelScope.launch {
            smartHomeSession.signOut()
            mutableUiState.value = mutableUiState.value.copy(isSigningOut = false, signedOut = true)
        }
    }

    private fun loadSummary() {
        viewModelScope.launch {
            mutableUiState.value = when (val result = userAccount.summary()) {
                is AccountSummaryResult.Success -> mutableUiState.value.copy(
                    tokenSuffix = result.account.tokenSuffix,
                    expiresIn = result.account.expiresIn,
                )

                AccountSummaryResult.SessionMissing -> mutableUiState.value.copy(signedOut = true)
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeSettings.mode.collect { mode ->
                mutableUiState.value = mutableUiState.value.copy(themeMode = mode)
            }
        }
    }
}
