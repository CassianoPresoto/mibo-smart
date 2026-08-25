package intelbras.mobi.smart.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboFeedbackState(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    mark: @Composable () -> Unit = { MiboEmptyMark() },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        mark()
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MiboTheme.typography.subtitle.copy(fontSize = 17.sp),
            color = MiboTheme.colors.text,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = body,
            style = MiboTheme.typography.caption.copy(fontSize = 13.sp, lineHeight = 20.sp),
            color = MiboTheme.colors.muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(22.dp))
        MiboCompactButton(text = actionLabel, onClick = onAction)
    }
}

@Composable
fun MiboEmptyMark(modifier: Modifier = Modifier) {
    val outline = MiboTheme.colors.outline
    Box(
        modifier = modifier
            .size(56.dp)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                        ),
                    ),
                )
            },
    )
}

@Composable
fun MiboErrorMark(modifier: Modifier = Modifier) {
    val danger = MiboTheme.colors.danger
    Box(
        modifier = modifier
            .size(46.dp)
            .border(2.dp, danger, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "!",
            style = MiboTheme.typography.title.copy(fontSize = 22.sp),
            color = danger,
        )
    }
}


@Composable
fun MiboSkeletonList(
    label: String,
    modifier: Modifier = Modifier,
    itemCount: Int = 3,
    itemHeight: Int = 118,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(itemCount) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 700, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "skeletonAlpha$index",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(itemHeight.dp)
                    .alpha(alpha)
                    .clip(MiboSmartShapes.card)
                    .background(MiboTheme.colors.surface)
                    .border(MiboSmartSize.hairline, MiboTheme.colors.outline, MiboSmartShapes.card),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MiboTheme.typography.caption.copy(fontSize = 13.sp),
            color = MiboTheme.colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun MiboFeedbackStateEmptyPreview() {
    MiboTheme {
        Surface {
            MiboFeedbackState(
                title = "Nenhum dispositivo",
                body = "Esta conta ainda não tem dispositivos vinculados ou compartilhados.",
                actionLabel = "Atualizar",
                onAction = {},
            )
        }
    }
}

@Preview
@Composable
private fun MiboFeedbackStateErrorPreview() {
    MiboTheme {
        Surface {
            MiboFeedbackState(
                title = "Sem conexão",
                body = "Sem conexão com a plataforma. Verifique a internet e tente de novo.",
                actionLabel = "Tentar de novo",
                onAction = {},
                mark = { MiboErrorMark() },
            )
        }
    }
}

@Preview
@Composable
private fun MiboSkeletonListPreview() {
    MiboTheme {
        Surface {
            MiboSkeletonList(
                label = "Buscando seus dispositivos…",
                itemCount = 2,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
