package intelbras.mobi.smart.ui.feature.video.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.domain.playback.model.PlaybackSource
import intelbras.mobi.smart.ui.component.MiboBackButton
import intelbras.mobi.smart.ui.component.MiboVideoControlButton
import intelbras.mobi.smart.ui.feature.video.VideoPlayerSurface
import intelbras.mobi.smart.ui.feature.video.component.CapturePreview
import intelbras.mobi.smart.ui.feature.video.rememberVideoPlayer
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.capture_library_back
import mibosmart.shared.generated.resources.capture_library_clip
import mibosmart.shared.generated.resources.capture_library_close
import mibosmart.shared.generated.resources.capture_library_delete
import mibosmart.shared.generated.resources.capture_library_empty
import mibosmart.shared.generated.resources.capture_library_loading
import mibosmart.shared.generated.resources.capture_library_photo
import mibosmart.shared.generated.resources.capture_library_subtitle
import mibosmart.shared.generated.resources.capture_library_title
import org.jetbrains.compose.resources.stringResource

private const val GRID_COLUMNS = 2
private const val PREVIEW_RATIO = 4f / 3f

@Composable
internal fun CaptureLibraryScreen(
    uiState: CaptureLibraryUiState,
    cameraName: String,
    selectedCaptureId: String?,
    loadPreview: suspend (String) -> ByteArray?,
    pathOf: (String) -> String,
    onCaptureRemoved: (String) -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    var openCaptureId by remember(selectedCaptureId) { mutableStateOf(selectedCaptureId) }
    val openCapture = uiState.captures.firstOrNull { capture -> capture.id == openCaptureId }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            CaptureLibraryHeader(cameraName = cameraName, onLeave = onLeave)

            when {
                uiState.isLoading -> LibraryNote(stringResource(Res.string.capture_library_loading))
                uiState.isEmpty -> LibraryNote(stringResource(Res.string.capture_library_empty))
                else -> CaptureGrid(
                    captures = uiState.captures,
                    loadPreview = loadPreview,
                    onCaptureClick = { capture -> openCaptureId = capture.id },
                )
            }
        }

        openCapture?.let { capture ->
            CaptureViewer(
                capture = capture,
                loadPreview = loadPreview,
                pathOf = pathOf,
                onDelete = {
                    openCaptureId = null
                    onCaptureRemoved(capture.id)
                },
                onClose = { openCaptureId = null },
            )
        }
    }
}

@Composable
private fun CaptureLibraryHeader(cameraName: String, onLeave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MiboSmartSize.listPadding,
                end = MiboSmartSize.listPadding,
                top = MiboSmartSpacing.md,
                bottom = MiboSmartSpacing.sm,
            ),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiboBackButton(
            description = stringResource(Res.string.capture_library_back),
            onClick = onLeave,
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.capture_library_title),
                style = MiboTheme.typography.title,
                color = MiboTheme.colors.text,
                maxLines = 1,
            )
            Text(
                text = stringResource(Res.string.capture_library_subtitle, cameraName),
                style = MiboTheme.typography.caption,
                color = MiboTheme.colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CaptureGrid(
    captures: List<CameraCaptureUiModel>,
    loadPreview: suspend (String) -> ByteArray?,
    onCaptureClick: (CameraCaptureUiModel) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = MiboSmartSize.listPadding,
            end = MiboSmartSize.listPadding,
            bottom = MiboSmartSpacing.lg,
        ),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
    ) {
        items(captures, key = { capture -> capture.id }) { capture ->
            CaptureCell(capture = capture, loadPreview = loadPreview, onClick = { onCaptureClick(capture) })
        }
    }
}

@Composable
private fun CaptureCell(
    capture: CameraCaptureUiModel,
    loadPreview: suspend (String) -> ByteArray?,
    onClick: () -> Unit,
) {
    val colors = MiboTheme.colors
    Column(
        modifier = Modifier
            .clip(MiboSmartShapes.icon)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.icon)
            .clickable(onClick = onClick),
    ) {
        CapturePreview(
            previewFileName = capture.previewFileName,
            kind = capture.kind,
            loadPreview = loadPreview,
            modifier = Modifier.fillMaxWidth().aspectRatio(PREVIEW_RATIO),
        )
        Column(Modifier.padding(MiboSmartSpacing.sm)) {
            Text(
                text = capture.kindLabel(),
                style = MiboTheme.typography.label,
                color = colors.muted,
            )
            Spacer(Modifier.height(MiboSmartSpacing.xxs))
            Text(
                text = capture.momentLabel,
                style = MiboTheme.typography.body.copy(fontSize = 13.sp),
                color = colors.text,
                maxLines = 1,
            )
            Text(
                text = capture.detailLabel(),
                style = MiboTheme.typography.monoSmall,
                color = colors.muted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CaptureViewer(
    capture: CameraCaptureUiModel,
    loadPreview: suspend (String) -> ByteArray?,
    pathOf: (String) -> String,
    onDelete: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = MiboTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.videoBackdrop)
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        when (capture.kind) {
            CameraCaptureKindUiModel.Photo -> CapturePreview(
                previewFileName = capture.fileName,
                kind = capture.kind,
                loadPreview = loadPreview,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().aspectRatio(PREVIEW_RATIO),
            )

            CameraCaptureKindUiModel.Clip -> ClipPlayer(
                path = pathOf(capture.fileName),
                modifier = Modifier.fillMaxWidth().aspectRatio(PREVIEW_RATIO),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(MiboSmartSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.sm),
        ) {
            MiboVideoControlButton(
                icon = Icons.Filled.Delete,
                description = stringResource(Res.string.capture_library_delete),
                onClick = onDelete,
                tint = colors.danger,
            )
            MiboVideoControlButton(
                icon = Icons.Filled.Close,
                description = stringResource(Res.string.capture_library_close),
                onClick = onClose,
            )
        }

        Text(
            text = "${capture.momentLabel} · ${capture.detailLabel()}",
            style = MiboTheme.typography.caption.copy(fontSize = 12.sp),
            color = colors.onVideo,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(MiboSmartSpacing.lg),
        )
    }
}

@Composable
private fun ClipPlayer(path: String, modifier: Modifier = Modifier) {
    val player = rememberVideoPlayer()

    LaunchedEffect(path) { player.start(PlaybackSource.RecordedClip(fileUrlOf(path))) }
    DisposableEffect(path) { onDispose { player.stop() } }

    VideoPlayerSurface(player = player, modifier = modifier)
}

@Composable
private fun LibraryNote(text: String) {
    Text(
        text = text,
        style = MiboTheme.typography.body,
        color = MiboTheme.colors.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MiboSmartSize.screenPadding, vertical = MiboSmartSpacing.xl),
    )
}

@Composable
private fun CameraCaptureUiModel.kindLabel(): String = when (kind) {
    CameraCaptureKindUiModel.Photo -> stringResource(Res.string.capture_library_photo)
    CameraCaptureKindUiModel.Clip -> stringResource(Res.string.capture_library_clip)
}

private fun CameraCaptureUiModel.detailLabel(): String =
    if (durationLabel.isEmpty()) sizeLabel else "$durationLabel · $sizeLabel"

internal fun fileUrlOf(path: String): String = "file://$path"

@Preview
@Composable
private fun CaptureLibraryScreenPreview() {
    MiboTheme {
        CaptureLibraryScreen(
            uiState = CaptureLibraryUiState(
                isLoading = false,
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
            ),
            cameraName = "Sala",
            selectedCaptureId = null,
            loadPreview = { null },
            pathOf = { it },
            onCaptureRemoved = {},
            onLeave = {},
        )
    }
}

@Preview
@Composable
private fun CaptureLibraryScreenEmptyPreview() {
    MiboTheme {
        CaptureLibraryScreen(
            uiState = CaptureLibraryUiState(isLoading = false),
            cameraName = "Sala",
            selectedCaptureId = null,
            loadPreview = { null },
            pathOf = { it },
            onCaptureRemoved = {},
            onLeave = {},
        )
    }
}
