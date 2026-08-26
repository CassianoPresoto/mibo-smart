package intelbras.mobi.smart.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    monospace: Boolean = false,
) {
    val colors = MiboTheme.colors
    HorizontalDivider(color = colors.outline, thickness = MiboSmartSize.hairline)
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MiboTheme.typography.body,
            color = colors.muted,
        )
        Text(
            text = value,
            style = if (monospace) {
                MiboTheme.typography.mono.copy(
                    fontSize = 14.sp,
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                MiboTheme.typography.body.copy(fontWeight = FontWeight.SemiBold)
            },
            color = colors.text,
        )
    }
}

@Preview
@Composable
private fun MiboDetailRowPreview() {
    MiboTheme {
        Surface {
            MiboDetailRow(label = "Expira em", value = "1h 42min")
        }
    }
}
