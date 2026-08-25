package intelbras.mobi.smart.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class MiboTypography(
    val display: TextStyle,
    val title: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val caption: TextStyle,
    val label: TextStyle,
    val button: TextStyle,
    val mono: TextStyle,
    val monoSmall: TextStyle,
)


fun miboTypography(
    sans: FontFamily = FontFamily.Default,
    mono: FontFamily = FontFamily.Monospace,
) = MiboTypography(
    display = TextStyle(
        fontFamily = sans,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.7).sp,
    ),
    title = TextStyle(
        fontFamily = sans,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp,
    ),
    subtitle = TextStyle(
        fontFamily = sans,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp,
    ),
    body = TextStyle(
        fontFamily = sans,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
    ),
    caption = TextStyle(
        fontFamily = sans,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    label = TextStyle(
        fontFamily = sans,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.9.sp,
    ),
    button = TextStyle(
        fontFamily = sans,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
    ),
    mono = TextStyle(
        fontFamily = mono,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 1.sp,
    ),
    monoSmall = TextStyle(
        fontFamily = mono,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    ),
)