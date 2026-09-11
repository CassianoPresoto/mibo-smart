package intelbras.mobi.smart.ui.feature.video.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.component.MiboCard
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureKindUiModel
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureUiModel
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.capture_library_empty
import mibosmart.shared.generated.resources.capture_library_open
import mibosmart.shared.generated.resources.capture_library_see_all
import mibosmart.shared.generated.resources.capture_library_title
import org.jetbrains.compose.resources.stringResource

private val thumbnailWidth = 112.dp
private val thumbnailHeight = 74.dp
private const val CARD_CAPTURE_LIMIT = 10

@Composable
internal fun CaptureLibraryCard(
    captures: List<CameraCaptureUiModel>,
    loadPreview: suspend (String) -> ByteArray?,
    onCaptureClick: (CameraCaptureUiModel) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors

    MiboCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.capture_library_title),
                style = MiboTheme.typography.subtitle,
                color = colors.text,
            )
            if (captures.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.capture_library_see_all),
                    style = MiboTheme.typography.button.copy(fontSize = 13.sp),
                    color = colors.primary,
                    modifier = Modifier
                        .clip(MiboSmartShapes.code)
                        .clickable(onClick = onSeeAll)
                        .padding(horizontal = MiboSmartSpacing.xs, vertical = MiboSmartSpacing.xxs),
                )
            }
        }

        Spacer(Modifier.height(MiboSmartSpacing.md))

        if (captures.isEmpty()) {
            Text(
                text = stringResource(Res.string.capture_library_empty),
                style = MiboTheme.typography.body,
                color = colors.muted,
            )
            return@MiboCard
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm)) {
            items(captures.take(CARD_CAPTURE_LIMIT), key = { capture -> capture.id }) { capture ->
                CaptureThumbnail(
                    capture = capture,
                    loadPreview = loadPreview,
                    onClick = { onCaptureClick(capture) },
                )
            }
        }
    }
}

@Composable
private fun CaptureThumbnail(
    capture: CameraCaptureUiModel,
    loadPreview: suspend (String) -> ByteArray?,
    onClick: () -> Unit,
) {
    val colors = MiboTheme.colors
    val openLabel = stringResource(Res.string.capture_library_open)

    Column(
        modifier = Modifier
            .width(thumbnailWidth)
            .clip(MiboSmartShapes.small)
            .clickable(onClick = onClick)
            .semantics { contentDescription = openLabel },
    ) {
        Box {
            CapturePreview(
                previewFileName = capture.previewFileName,
                kind = capture.kind,
                loadPreview = loadPreview,
                modifier = Modifier
                    .size(width = thumbnailWidth, height = thumbnailHeight)
                    .clip(MiboSmartShapes.small),
            )
            if (capture.durationLabel.isNotEmpty()) {
                Text(
                    text = capture.durationLabel,
                    style = MiboTheme.typography.monoSmall,
                    color = colors.onVideo,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(MiboSmartSpacing.xxs)
                        .clip(MiboSmartShapes.code)
                        .background(colors.videoScrim)
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(MiboSmartSpacing.xxs))
        Text(
            text = capture.momentLabel,
            style = MiboTheme.typography.caption.copy(fontSize = 11.5.sp),
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun CaptureLibraryCardPreview() {
    MiboTheme {
        Surface {
            CaptureLibraryCard(
                captures = listOf(
                    CameraCaptureUiModel(
                        id = "1",
                        kind = CameraCaptureKindUiModel.Photo,
                        fileName = "foto.jpg",
                        previewFileName = "foto.jpg",
                        momentLabel = "25/08 · 09:30",
                        durationLabel = "",
                        sizeLabel = "0,8 MB",
                    ),
                    CameraCaptureUiModel(
                        id = "2",
                        kind = CameraCaptureKindUiModel.Clip,
                        fileName = "take.mp4",
                        previewFileName = "take-capa.jpg",
                        momentLabel = "25/08 · 09:28",
                        durationLabel = "00:12",
                        sizeLabel = "4,2 MB",
                    ),
                ),
                loadPreview = { null },
                onCaptureClick = {},
                onSeeAll = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun CaptureLibraryCardEmptyPreview() {
    MiboTheme {
        Surface {
            CaptureLibraryCard(
                captures = emptyList(),
                loadPreview = { null },
                onCaptureClick = {},
                onSeeAll = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
