package intelbras.mobi.smart

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import intelbras.mobi.smart.ui.navigation.AppNavHost

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface {
            AppNavHost()
        }
    }
}
