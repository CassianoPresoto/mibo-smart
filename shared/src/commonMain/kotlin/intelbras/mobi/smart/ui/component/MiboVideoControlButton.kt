package intelbras.mobi.smart.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.ui.theme.MiboTheme

private val controlSize = 44.dp
private val controlIconSize = 20.dp
private const val DISABLED_ALPHA = 0.4f

@Composable
fun MiboVideoControlButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MiboTheme.colors.onVideo,
) {
    val colors = MiboTheme.colors
    Box(
        modifier = modifier
            .size(controlSize)
            .clip(CircleShape)
            .background(colors.videoControl)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = DISABLED_ALPHA),
            modifier = Modifier.size(controlIconSize),
        )
    }
}

@Preview
@Composable
private fun MiboVideoControlButtonPreview() {
    MiboTheme {
        Surface(color = MiboTheme.colors.videoBackdrop) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MiboVideoControlButton(
                    icon = Icons.Filled.FiberManualRecord,
                    description = "Gravar take",
                    onClick = {},
                    tint = MiboTheme.colors.live,
                )
                MiboVideoControlButton(
                    icon = Icons.Filled.PhotoCamera,
                    description = "Tirar foto",
                    onClick = {},
                )
                MiboVideoControlButton(
                    icon = Icons.Filled.PhotoCamera,
                    description = "Tirar foto",
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}
