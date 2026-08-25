package intelbras.mobi.smart.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

private object Palette {
    val Mist = Color(0xFFF2F5F2)
    val White = Color(0xFFFFFFFF)
    val Frost = Color(0xFFF7FAF7)
    val Fog = Color(0xFFE2E9E3)
    val Ink = Color(0xFF0F1512)
    val Slate = Color(0xFF64756B)
    val Green = Color(0xFF02A94D)
    val GreenDeep = Color(0xFF017A38)
    val GreenWash = Color(0xFFE4F6EB)
    val Red = Color(0xFFD64545)
    val Amber = Color(0xFFB5810F)
    val Stone = Color(0xFFEEF2EE)
    val Pitch = Color(0xFF0B100D)
    val Charcoal = Color(0xFF131A16)
    val Slate900 = Color(0xFF182119)
    val Graphite = Color(0xFF26312A)
    val Chalk = Color(0xFFE9F1EB)
    val Sage = Color(0xFF90A498)
    val GreenBright = Color(0xFF17BE60)
    val GreenMid = Color(0xFF0E9C4C)
    val GreenDeepWash = Color(0xFF12301E)
    val Coral = Color(0xFFF2726F)
    val AmberSoft = Color(0xFFE0AA4E)
    val Onyx = Color(0xFF101815)

    val Live = Color(0xFFFF4D4D)
    val VideoBackdrop = Color(0xFF0A0E0C)
    val Black = Color(0xFF000000)
}

@Immutable
data class MiboSmartColor(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val outline: Color,
    val text: Color,
    val muted: Color,
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primaryTint: Color,
    val danger: Color,
    val warning: Color,
    val codeSurface: Color,
    val statusOnline: Color,
    val statusOffline: Color,
    val live: Color,
    val videoBackdrop: Color,
    val videoScrim: Color,
    val videoControl: Color,
    val onVideo: Color,
    val shadow: Color,
    val isDark: Boolean,
)

val MiboLightColors = MiboSmartColor(
    background = Palette.Mist,
    surface = Palette.White,
    surfaceVariant = Palette.Frost,
    outline = Palette.Fog,
    text = Palette.Ink,
    muted = Palette.Slate,

    primary = Palette.Green,
    primaryPressed = Palette.GreenDeep,
    onPrimary = Palette.White,
    primaryTint = Palette.GreenWash,

    danger = Palette.Red,
    warning = Palette.Amber,
    codeSurface = Palette.Stone,

    statusOnline = Palette.Green,
    statusOffline = Palette.Slate,

    live = Palette.Live,
    videoBackdrop = Palette.VideoBackdrop,
    videoScrim = Palette.Black.copy(alpha = 0.55f),
    videoControl = Palette.White.copy(alpha = 0.16f),
    onVideo = Palette.White,

    shadow = Palette.Ink,
    isDark = false,
)

val MiboDarkColors = MiboSmartColor(
    background = Palette.Pitch,
    surface = Palette.Charcoal,
    surfaceVariant = Palette.Slate900,
    outline = Palette.Graphite,
    text = Palette.Chalk,
    muted = Palette.Sage,
    primary = Palette.GreenBright,
    primaryPressed = Palette.GreenMid,
    onPrimary = Palette.Pitch,
    primaryTint = Palette.GreenDeepWash,
    danger = Palette.Coral,
    warning = Palette.AmberSoft,
    codeSurface = Palette.Onyx,
    statusOnline = Palette.GreenBright,
    statusOffline = Palette.Sage,
    live = Palette.Live,
    videoBackdrop = Palette.VideoBackdrop,
    videoScrim = Palette.Black.copy(alpha = 0.55f),
    videoControl = Palette.White.copy(alpha = 0.16f),
    onVideo = Palette.White,
    shadow = Palette.Black,
    isDark = true,
)