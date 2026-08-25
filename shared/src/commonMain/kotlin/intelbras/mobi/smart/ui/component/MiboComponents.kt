package intelbras.mobi.smart.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surface,
        label = "chipBackground",
    )
    val content by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.text,
        label = "chipContent",
    )
    Text(
        text = label,
        style = MiboTheme.typography.button.copy(
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold
        ),
        color = content,
        maxLines = 1,
        modifier = modifier.clip(MiboSmartShapes.pill).background(background)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.pill).clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
fun MiboCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    Text(
        text = text,
        style = MiboTheme.typography.button.copy(fontSize = 14.sp),
        color = colors.onPrimary,
        modifier = modifier.clip(MiboSmartShapes.medium).background(colors.primary)
            .clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 12.dp),
    )
}

@Composable
fun MiboSquareIconButton(
    symbol: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MiboTheme.colors
    Box(
        modifier = modifier.size(34.dp).clip(MiboSmartShapes.small).background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.small)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            fontSize = 15.sp,
            color = if (enabled) colors.text else colors.muted,
        )
    }
}

@Composable
fun MiboAccountAvatar(
    initials: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    Box(
        modifier = modifier.size(38.dp).clip(CircleShape).background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, CircleShape).clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
        )
    }
}

@Composable
fun MiboCodeChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    Text(
        text = text,
        style = MiboTheme.typography.monoSmall,
        color = colors.muted,
        maxLines = 1,
        modifier = modifier.clip(MiboSmartShapes.code).background(colors.codeSurface)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
fun MiboStatusLabel(
    label: String,
    online: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    val tone = if (online) colors.statusOnline else colors.statusOffline
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            Modifier.size(7.dp).clip(CircleShape).background(tone),
        )
        Text(
            text = label.uppercase(),
            style = MiboTheme.typography.label.copy(letterSpacing = 0.3.sp),
            color = tone,
            maxLines = 1,
        )
    }
}

@Composable
fun MiboOutlinedBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    Text(
        text = label.uppercase(),
        style = MiboTheme.typography.label.copy(fontSize = 10.sp, letterSpacing = 0.4.sp),
        color = colors.warning,
        maxLines = 1,
        modifier = modifier.border(MiboSmartSize.hairline, colors.warning, MiboSmartShapes.code)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

@Preview
@Composable
private fun MiboComponentsPreview() {
    MiboTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiboFilterChip(label = "Selecionado", selected = true, onClick = {})
                    MiboFilterChip(label = "Não selecionado", selected = false, onClick = {})
                }
                MiboCompactButton(text = "Ação", onClick = {})
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiboSquareIconButton(symbol = "⚙", description = "Configurações", onClick = {})
                    MiboAccountAvatar(initials = "MS", description = "Conta", onClick = {})
                }
                MiboCodeChip(text = "ABC-123")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MiboStatusLabel(label = "Online", online = true)
                    MiboStatusLabel(label = "Offline", online = false)
                }
                MiboOutlinedBadge(label = "Beta")
            }
        }
    }
}
