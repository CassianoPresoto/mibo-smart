package intelbras.mobi.smart.ui.feature.lock.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme

private val glyphSize = 48.dp
private val spinnerStroke = 3.dp
private const val SPINNER_TRACK_ALPHA = 0.25f

@Composable
fun LockDial(
    label: String,
    description: String,
    open: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val colors = MiboTheme.colors
    val background by animateColorAsState(
        targetValue = if (open) colors.primary else colors.primaryTint,
        label = "lockDialBackground",
    )
    val content by animateColorAsState(
        targetValue = if (open) colors.onPrimary else colors.primary,
        label = "lockDialContent",
    )

    Box(
        modifier = modifier.size(MiboSmartSize.lockDial),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(background)
                .then(
                    if (loading) {
                        Modifier
                    } else {
                        Modifier.border(MiboSmartSize.borderWidth, colors.primary, CircleShape)
                    }
                )
                .clickable(enabled = enabled, onClick = onClick)
                .semantics { contentDescription = description },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                MiboSmartSpacing.sm,
                Alignment.CenterVertically,
            ),
        ) {
            Crossfade(targetState = open, label = "lockDialGlyph") { isOpen ->
                Icon(
                    imageVector = if (isOpen) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(glyphSize),
                )
            }
            Text(
                text = label,
                style = MiboTheme.typography.subtitle.copy(fontSize = 15.sp),
                color = content,
            )
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                color = content,
                strokeWidth = spinnerStroke,
                trackColor = content.copy(alpha = SPINNER_TRACK_ALPHA),
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Preview
@Composable
private fun LockDialClosedPreview() {
    LockDialPreview(open = false, label = "Trancada", enabled = true)
}

@Preview
@Composable
private fun LockDialOpenPreview() {
    LockDialPreview(open = true, label = "Destrancada", enabled = true)
}

@Preview
@Composable
private fun LockDialSendingPreview() {
    MiboTheme {
        Surface(color = MiboTheme.colors.background) {
            LockDial(
                label = "Enviando…",
                description = "Trancar ou destrancar a fechadura",
                open = false,
                enabled = false,
                loading = true,
                onClick = {},
                modifier = Modifier.padding(MiboSmartSpacing.lg),
            )
        }
    }
}

@Preview
@Composable
private fun LockDialLockingPreview() {
    MiboTheme {
        Surface(color = MiboTheme.colors.background) {
            LockDial(
                label = "Enviando…",
                description = "Trancar ou destrancar a fechadura",
                open = true,
                enabled = false,
                loading = true,
                onClick = {},
                modifier = Modifier.padding(MiboSmartSpacing.lg),
            )
        }
    }
}

@Preview
@Composable
private fun LockDialCheckingPreview() {
    LockDialPreview(open = false, label = "Verificando…", enabled = false)
}

@Preview
@Composable
private fun LockDialDarkPreview() {
    MiboTheme(darkTheme = true) {
        Surface(color = MiboTheme.colors.background) {
            LockDial(
                label = "Destrancada",
                description = "Trancar ou destrancar a fechadura",
                open = true,
                enabled = true,
                onClick = {},
                modifier = Modifier.padding(MiboSmartSpacing.lg),
            )
        }
    }
}

@Composable
private fun LockDialPreview(open: Boolean, label: String, enabled: Boolean) {
    MiboTheme {
        Surface(color = MiboTheme.colors.background) {
            LockDial(
                label = label,
                description = "Trancar ou destrancar a fechadura",
                open = open,
                enabled = enabled,
                onClick = {},
                modifier = Modifier.padding(MiboSmartSpacing.lg),
            )
        }
    }
}
