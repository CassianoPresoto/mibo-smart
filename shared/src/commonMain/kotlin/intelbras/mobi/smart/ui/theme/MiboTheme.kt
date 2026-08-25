package intelbras.mobi.smart.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.sp

private val LocalMiboColors = staticCompositionLocalOf { MiboLightColors }
private val LocalMiboTypography = staticCompositionLocalOf { miboTypography() }

object MiboTheme {
    val colors: MiboSmartColor
        @Composable @ReadOnlyComposable get() = LocalMiboColors.current

    val typography: MiboTypography
        @Composable @ReadOnlyComposable get() = LocalMiboTypography.current
}

@Composable
fun MiboTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: MiboSmartColor = if (darkTheme) MiboDarkColors else MiboLightColors,
    typography: MiboTypography = miboTypography(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMiboColors provides colors,
        LocalMiboTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = typography.toTypography(),
            shapes = miboMaterialShapes,
            content = content,
        )
    }
}

private fun MiboSmartColor.toColorScheme() = if (isDark) {
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryTint,
        onPrimaryContainer = text,
        secondary = primary,
        onSecondary = onPrimary,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = muted,
        surfaceContainer = surfaceVariant,
        outline = outline,
        outlineVariant = outline,
        error = danger,
        onError = background,
        errorContainer = surfaceVariant,
        onErrorContainer = danger,
        scrim = videoScrim,
    )
} else {
    lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryTint,
        onPrimaryContainer = text,
        secondary = primary,
        onSecondary = onPrimary,
        background = background,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = muted,
        surfaceContainer = surfaceVariant,
        outline = outline,
        outlineVariant = outline,
        error = danger,
        onError = onPrimary,
        errorContainer = surfaceVariant,
        onErrorContainer = danger,
        scrim = videoScrim,
    )
}

private fun MiboTypography.toTypography() = Typography(
    displaySmall = display,
    headlineMedium = display,
    headlineSmall = title,
    titleLarge = title,
    titleMedium = subtitle,
    titleSmall = subtitle.copy(fontSize = 14.sp),
    bodyLarge = body,
    bodyMedium = body.copy(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = caption,
    labelLarge = button,
    labelMedium = button.copy(fontSize = 13.sp),
    labelSmall = label,
)

private val miboMaterialShapes = Shapes(
    extraSmall = MiboSmartShapes.code,
    small = MiboSmartShapes.small,
    medium = MiboSmartShapes.field,
    large = MiboSmartShapes.card,
    extraLarge = MiboSmartShapes.panel,
)