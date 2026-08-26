package intelbras.mobi.smart.ui.component

import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MiboTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier.semantics { contentDescription = description },
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onPrimary,
            checkedTrackColor = colors.primary,
            checkedBorderColor = colors.primary,
            uncheckedThumbColor = colors.surface,
            uncheckedTrackColor = colors.outline,
            uncheckedBorderColor = colors.outline,
        ),
    )
}

@Preview
@Composable
private fun MiboSwitchPreview() {
    MiboTheme {
        Surface {
            MiboSwitch(checked = true, onCheckedChange = {}, description = "Tema escuro")
        }
    }
}
