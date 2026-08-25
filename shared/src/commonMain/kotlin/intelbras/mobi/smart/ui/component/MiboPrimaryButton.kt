package intelbras.mobi.smart.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = MiboTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(MiboSmartSize.control),
        enabled = enabled && !loading,
        shape = MiboSmartShapes.button,
        contentPadding = PaddingValues(horizontal = 16.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = if (loading) colors.primary else colors.primary.copy(alpha = 0.35f),
            disabledContentColor = if (loading) colors.onPrimary else colors.onPrimary.copy(alpha = 0.7f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = colors.onPrimary,
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
private fun MiboPrimaryButtonPreview() {
    MiboPrimaryButton(
        text = "MiboPrimaryButton",
        onClick = {}
    )
}

