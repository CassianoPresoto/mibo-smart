package intelbras.mobi.smart.ui.feature.account

import kotlin.time.Duration

private const val TOKEN_MASK_LENGTH = 8
private const val MASK_GLYPH = '•'

internal fun maskedToken(suffix: String): String =
    MASK_GLYPH.toString().repeat(TOKEN_MASK_LENGTH) + suffix

internal fun formattedTimeLeft(duration: Duration): String {
    val totalMinutes = duration.inWholeMinutes
    if (totalMinutes <= 0) return "<1min"

    val hours = totalMinutes / MINUTES_IN_HOUR
    val minutes = totalMinutes % MINUTES_IN_HOUR
    return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
}

private const val MINUTES_IN_HOUR = 60L
