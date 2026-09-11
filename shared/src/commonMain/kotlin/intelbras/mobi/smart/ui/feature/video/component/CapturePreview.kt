package intelbras.mobi.smart.ui.feature.video.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import intelbras.mobi.smart.ui.feature.video.capture.CameraCaptureKindUiModel
import intelbras.mobi.smart.ui.theme.MiboTheme
import org.jetbrains.compose.resources.decodeToImageBitmap

private val placeholderIconSize = 22.dp

@Composable
internal fun CapturePreview(
    previewFileName: String,
    kind: CameraCaptureKindUiModel,
    loadPreview: suspend (String) -> ByteArray?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val colors = MiboTheme.colors
    var preview by remember(previewFileName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(previewFileName) {
        preview = previewFileName
            .takeIf { name -> name.isNotBlank() }
            ?.let { name -> loadPreview(name) }
            ?.toImageBitmapOrNull()
    }

    Box(
        modifier = modifier.background(colors.videoBackdrop),
        contentAlignment = Alignment.Center,
    ) {
        val frame = preview
        if (frame == null) {
            Icon(
                imageVector = kind.placeholderIcon(),
                contentDescription = null,
                tint = colors.onVideo.copy(alpha = PLACEHOLDER_ALPHA),
                modifier = Modifier.size(placeholderIconSize),
            )
            return@Box
        }
        Image(
            bitmap = frame,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private const val PLACEHOLDER_ALPHA = 0.55f

private fun CameraCaptureKindUiModel.placeholderIcon() = when (this) {
    CameraCaptureKindUiModel.Photo -> Icons.Filled.PhotoCamera
    CameraCaptureKindUiModel.Clip -> Icons.Filled.Videocam
}

private fun ByteArray.toImageBitmapOrNull(): ImageBitmap? = try {
    decodeToImageBitmap()
} catch (failure: IllegalArgumentException) {
    null
}
