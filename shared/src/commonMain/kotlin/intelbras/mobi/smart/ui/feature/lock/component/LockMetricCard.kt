package intelbras.mobi.smart.ui.feature.lock.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun LockMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MiboTheme.colors.text,
    monospace: Boolean = false,
) {
    val colors = MiboTheme.colors
    Column(
        modifier = modifier
            .clip(MiboSmartShapes.icon)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.icon)
            .padding(MiboSmartSpacing.md),
    ) {
        Text(
            text = label.uppercase(),
            style = MiboTheme.typography.label.copy(letterSpacing = 0.7.sp),
            color = colors.muted,
            maxLines = 1,
        )
        Spacer(Modifier.height(MiboSmartSpacing.xxs))
        Text(
            text = value,
            style = if (monospace) {
                MiboTheme.typography.monoSmall.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            } else {
                MiboTheme.typography.subtitle.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            },
            color = valueColor,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun LockMetricCardPreview() {
    MiboTheme {
        Surface(color = MiboTheme.colors.background) {
            MetricRow()
        }
    }
}

@Preview
@Composable
private fun LockMetricCardDarkPreview() {
    MiboTheme(darkTheme = true) {
        Surface(color = MiboTheme.colors.background) {
            MetricRow()
        }
    }
}

@Composable
private fun MetricRow() {
    Row(
        modifier = Modifier.padding(MiboSmartSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
    ) {
        LockMetricCard(
            label = "Bateria",
            value = "98%",
            modifier = Modifier.weight(1f),
        )
        LockMetricCard(
            label = "Sinal",
            value = "4/5",
            monospace = true,
            modifier = Modifier.weight(1f),
        )
        LockMetricCard(
            label = "Remoto",
            value = "Ativo",
            valueColor = MiboTheme.colors.primary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun LockMetricCardUnavailablePreview() {
    MiboTheme {
        Surface(color = MiboTheme.colors.background) {
            Row(
                modifier = Modifier.padding(MiboSmartSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
            ) {
                LockMetricCard(
                    label = "Bateria",
                    value = "—",
                    modifier = Modifier.weight(1f),
                )
                LockMetricCard(
                    label = "Sinal",
                    value = "—",
                    monospace = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
