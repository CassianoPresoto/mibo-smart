package intelbras.mobi.smart

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import intelbras.mobi.smart.ui.navigation.AppNavHost
import intelbras.mobi.smart.ui.theme.AppThemeViewModel
import intelbras.mobi.smart.ui.theme.MiboTheme
import intelbras.mobi.smart.ui.theme.resolvesToDark
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(themeViewModel: AppThemeViewModel = koinViewModel()) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

    MiboTheme(darkTheme = themeMode.resolvesToDark()) {
        Surface {
            AppNavHost()
        }
    }
}
