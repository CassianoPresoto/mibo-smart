package intelbras.mobi.smart.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = MiboTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(MiboSmartSize.control),
        enabled = enabled && !loading,
        shape = MiboSmartShapes.button,
        contentPadding = PaddingValues(horizontal = 16.dp),
        border = BorderStroke(MiboSmartSize.borderWidth, SolidColor(colors.danger)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.danger,
            disabledContentColor = colors.danger.copy(alpha = 0.5f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.danger,
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MiboTheme.typography.button,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun MiboDangerButtonPreview() {
    MiboTheme {
        Surface {
            MiboDangerButton(text = "Sair", onClick = {})
        }
    }
}
