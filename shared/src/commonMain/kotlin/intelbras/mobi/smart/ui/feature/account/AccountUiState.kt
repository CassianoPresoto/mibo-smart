package intelbras.mobi.smart.ui.feature.account

import androidx.compose.runtime.Immutable
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import kotlin.time.Duration

@Immutable
data class AccountUiState(
    val tokenSuffix: String = "",
    val expiresIn: Duration? = null,
    val themeMode: ThemeMode = ThemeMode.System,
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false,
) {
    val hasSession: Boolean get() = tokenSuffix.isNotBlank()
}
