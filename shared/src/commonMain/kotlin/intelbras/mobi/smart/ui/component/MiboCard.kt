package intelbras.mobi.smart.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MiboTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MiboSmartShapes.card)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.card)
            .padding(16.dp),
        content = content,
    )
}

@Preview
@Composable
private fun MiboCardPreview() {
    MiboTheme {
        Surface {
            MiboCard(modifier = Modifier.padding(16.dp)) {
                Text(text = "Sessão", style = MiboTheme.typography.subtitle)
                MiboDetailRow(label = "Expira em", value = "1h 42min")
            }
        }
    }
}
