package intelbras.mobi.smart

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import intelbras.mobi.smart.ui.navigation.AppNavHost
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
@Preview
fun App() {
    MiboTheme {
        Surface {
            AppNavHost()
        }
    }
}
