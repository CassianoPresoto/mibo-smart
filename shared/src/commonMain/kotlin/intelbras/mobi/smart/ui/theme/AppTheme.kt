package intelbras.mobi.smart.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.ThemeSettings
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppThemeViewModel(themeSettings: ThemeSettings) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themeSettings.mode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.System,
    )
}

@Composable
fun ThemeMode.resolvesToDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}
