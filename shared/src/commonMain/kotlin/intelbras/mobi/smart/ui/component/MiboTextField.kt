package intelbras.mobi.smart.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartElevation
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    placeholder: String? = null,
    trailingLabel: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    trailingEnabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = MiboTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> colors.danger
            isFocused || value.isNotEmpty() -> colors.primary
            else -> colors.outline
        },
        label = "fieldBorder",
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(MiboSmartSize.control)
            .shadow(
                elevation = if (colors.isDark) MiboSmartElevation.none else MiboSmartElevation.card,
                shape = MiboSmartShapes.field,
                ambientColor = colors.shadow,
                spotColor = colors.shadow,
            )
            .clip(MiboSmartShapes.field)
            .background(colors.surface)
            .border(MiboSmartSize.borderWidth, borderColor, MiboSmartShapes.field),
        enabled = enabled,
        singleLine = true,
        textStyle = MiboTheme.typography.mono.copy(color = colors.text),
        cursorBrush = SolidColor(colors.primary),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = MiboTheme.typography.mono,
                            color = colors.muted,
                        )
                    }
                    innerTextField()
                }
                if (trailingLabel != null && onTrailingClick != null) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = trailingLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (trailingEnabled) colors.primary else colors.muted,
                        modifier = Modifier
                            .clip(MiboSmartShapes.pill)
                            .clickable(enabled = trailingEnabled, onClick = onTrailingClick)
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun MiboTextFieldPreview() {
    MiboTextField(
        value = "",
        onValueChange = {},
        placeholder = "Digite algo...",
    )
}

@Preview
@Composable
private fun MiboTextFieldWithTrailingPreview() {
    MiboTextField(
        value = "123456",
        onValueChange = {},
        placeholder = "Código",
        trailingLabel = "Enviar",
        onTrailingClick = {},
    )
}
