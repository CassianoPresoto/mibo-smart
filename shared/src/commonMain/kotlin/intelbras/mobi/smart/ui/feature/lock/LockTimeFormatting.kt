package intelbras.mobi.smart.ui.feature.lock

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

internal const val UNKNOWN_TIME = "—"

private const val TWO_DIGITS = 2
private const val PAD_GLYPH = '0'

internal fun Int.padded(): String = toString().padStart(TWO_DIGITS, PAD_GLYPH)

internal fun LocalDate.formatted(): String = "${day.padded()}/${monthNumber.padded()}/$year"

internal fun LocalDateTime.formattedTime(): String = "${hour.padded()}:${minute.padded()}"

internal fun LocalDateTime?.formattedMoment(): String =
    this?.let { "${it.date.formatted()} ${it.formattedTime()}" } ?: UNKNOWN_TIME
