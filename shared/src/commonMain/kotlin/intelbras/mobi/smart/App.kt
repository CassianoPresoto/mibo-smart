package intelbras.mobi.smart

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import intelbras.mobi.smart.ui.SmartHomeApp
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface {
            SmartHomeApp()
        }
    }
}
