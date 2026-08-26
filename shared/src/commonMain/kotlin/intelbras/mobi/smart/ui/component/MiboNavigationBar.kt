package intelbras.mobi.smart.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme

private val tabIconSize = 22.dp

@Composable
fun MiboNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MiboTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        HorizontalDivider(thickness = MiboSmartSize.hairline, color = colors.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MiboSmartSpacing.xs, vertical = MiboSmartSpacing.xs)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            content = content,
        )
    }
}

@Composable
fun RowScope.MiboNavigationTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MiboTheme.colors
    val tint by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.muted,
        label = "navigationTabTint",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = MiboSmartSpacing.xs)
            .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(tabIconSize),
        )
        Spacer(Modifier.height(MiboSmartSpacing.xxs))
        Text(
            text = label,
            style = MiboTheme.typography.label.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = tint,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun MiboNavigationBarPreview() {
    MiboTheme {
        Surface(color = MiboTheme.colors.background) {
            NavigationBarSample()
        }
    }
}

@Preview
@Composable
private fun MiboNavigationBarDarkPreview() {
    MiboTheme(darkTheme = true) {
        Surface(color = MiboTheme.colors.background) {
            NavigationBarSample()
        }
    }
}

@Composable
private fun NavigationBarSample() {
    MiboNavigationBar {
        MiboNavigationTab(
            label = "Dispositivos",
            icon = Icons.Filled.Devices,
            selected = true,
            onClick = {},
        )
        MiboNavigationTab(
            label = "Atividade",
            icon = Icons.Filled.History,
            selected = false,
            onClick = {},
        )
        MiboNavigationTab(
            label = "Conta",
            icon = Icons.Filled.Person,
            selected = false,
            onClick = {},
        )
    }
}
